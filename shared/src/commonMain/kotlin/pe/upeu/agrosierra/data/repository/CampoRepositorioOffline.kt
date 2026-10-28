package pe.upeu.agrosierra.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.local.ahoraMillis
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.CampoRepositorio
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

class CampoRepositorioOffline(
    private val local: BaseLocal,
    private val api: AgroApi,
    private val sesiones: SesionRepositorio,
    private val sincronizador: Sincronizador
) : CampoRepositorio {
    override fun observarLotes(): Flow<List<Lote>> = local.observarLotes()
    override fun observarLabores(loteId: Long): Flow<List<Labor>> = local.observarLabores(loteId)
    override suspend fun lote(id: Long): Lote? = local.lote(id)

    override suspend fun refrescarLotes(valle: String?): Int {
        val remotos = api.lotes(valle).map { it.aDominio() }
        local.reemplazarLotes(remotos)
        return remotos.size
    }

    override suspend fun refrescarLabores(loteId: Long): Int {
        val remotas = api.labores(loteId).map { it.aDominio() }
        local.reemplazarLaboresRemotas(loteId, remotas)
        return remotas.size
    }

    override suspend fun registrarLabor(
        loteId: Long,
        tipo: TipoLabor,
        fecha: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int
    ) {
        val sesion = sesiones.sesionActual() ?: throw AgroException.NoAutorizado()
        val lote = local.lote(loteId) ?: throw AgroException.Servidor("El lote no existe en la base local")
        if (lote.campaniaCerrada) throw AgroException.Conflicto("La campaña está cerrada")
        if (tipo == TipoLabor.FUMIGACION && cantidad > lote.dosisMaximaPorHectarea * lote.hectareas) {
            throw AgroException.Validacion(
                "La cantidad supera la dosis máxima del lote",
                mapOf("cantidad" to "Máximo: " + (lote.dosisMaximaPorHectarea * lote.hectareas))
            )
        }
        local.guardarLabor(
            Labor(
                id = local.siguienteIdTemporal(),
                loteId = loteId,
                tipo = tipo,
                fecha = fecha,
                responsable = sesion.nombre,
                responsableUsuario = sesion.usuario,
                cantidad = cantidad,
                cantidadJornales = cantidadJornales,
                observacion = observacion.trim(),
                pendiente = true,
                puedeEditar = true,
                operacionPendiente = OperacionPendiente.CREAR
            )
        )
    }

    override suspend fun actualizarLabor(labor: Labor) {
        val lote = local.lote(labor.loteId) ?: throw AgroException.Servidor("El lote no existe")
        if (lote.campaniaCerrada) throw AgroException.Conflicto("La campaña está cerrada")
        val operacion = if (labor.id < 0) OperacionPendiente.CREAR else OperacionPendiente.ACTUALIZAR
        local.actualizarLabor(labor.copy(pendiente = true, operacionPendiente = operacion))
    }

    override suspend fun eliminarLabor(labor: Labor) {
        if (labor.id < 0) {
            local.eliminarLabor(labor.id)
        } else {
            local.actualizarLabor(
                labor.copy(pendiente = true, operacionPendiente = OperacionPendiente.ELIMINAR, eliminado = true)
            )
        }
    }

    override suspend fun cerrarCampania(loteId: Long) {
        val actualizado = api.cerrarCampania(loteId).aDominio()
        local.guardarLote(actualizado)
    }

    override suspend fun sincronizar(): ResumenSincronizacion {
        var errores = 0
        var primerError: AgroException? = null
        val (enviados, descargados) = sincronizador.ejecutar(
            enviarPendientes = {
                var correctos = 0
                local.pendientes().forEach { pendiente ->
                    try {
                        when (pendiente.operacionPendiente) {
                            OperacionPendiente.CREAR -> {
                                val creado = api.crearLabor(pendiente).aDominio()
                                local.reemplazarTemporalPorRemota(pendiente.id, creado)
                            }
                            OperacionPendiente.ACTUALIZAR -> {
                                local.guardarLabor(api.actualizarLabor(pendiente).aDominio())
                            }
                            OperacionPendiente.ELIMINAR -> {
                                api.eliminarLabor(pendiente.id)
                                local.eliminarLabor(pendiente.id)
                            }
                            null -> Unit
                        }
                        correctos++
                    } catch (e: AgroException.NoAutorizado) {
                        throw e
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        errores++
                        if (primerError == null) {
                            primerError = e as? AgroException
                                ?: AgroException.Servidor(e.message ?: "No se pudo enviar un pendiente")
                        }
                    }
                }
                correctos
            },
            descargarServidor = {
                val lotes = api.lotes().map { it.aDominio() }
                local.reemplazarLotes(lotes)
                var total = lotes.size
                lotes.forEach { total += refrescarLabores(it.id) }
                total
            }
        )
        val ahora = ahoraMillis()
        local.guardarUltimaSincronizacion(ahora)
        return ResumenSincronizacion(enviados, descargados, errores, ahora, primerError)
    }

    override suspend fun ultimaSincronizacion(): Long? = local.ultimaSincronizacion()
}
