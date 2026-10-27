package pe.upeu.agrosierra.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import pe.upeu.agrosierra.AgroDb
import pe.upeu.agrosierra.Labor as LaborFila
import pe.upeu.agrosierra.Lote as LoteFila
import pe.upeu.agrosierra.domain.model.*

class BaseLocal(private val db: AgroDb) {
    private val q = db.agroDbQueries

    fun observarLotes(): Flow<List<Lote>> {
        val lotes = q.observarLotes().asFlow().mapToList(Dispatchers.Default)
            .map { lista -> lista.map { it.aDominio() } }
        val pendientes = q.observarPendientesPorLote().asFlow().mapToList(Dispatchers.Default)
            .map { filas -> filas.associate { it.loteId to it.cantidad } }
        return combine(lotes, pendientes) { lista, conteos ->
            lista.map { it.copy(pendientesSincronizar = conteos[it.id] ?: 0L) }
        }
    }

    fun observarLabores(loteId: Long): Flow<List<Labor>> =
        q.observarLaboresPorLote(loteId).asFlow().mapToList(Dispatchers.Default)
            .map { lista -> lista.map { it.aDominio() } }

    fun lote(id: Long): Lote? = q.obtenerLote(id).executeAsOneOrNull()?.aDominio()

    fun reemplazarLotes(remotos: List<Lote>) {
        db.transaction {
            q.borrarLotes()
            remotos.forEach(::guardarLote)
        }
    }

    fun guardarLote(lote: Lote) {
        q.guardarLote(
            lote.id, lote.codigo, lote.nombre, lote.hectareas, lote.cultivo,
            lote.valle, lote.campaniaCerrada.aEntero(), lote.dosisMaximaPorHectarea
        )
    }

    fun reemplazarLaboresRemotas(loteId: Long, remotas: List<Labor>) {
        db.transaction {
            q.borrarLaboresConfirmadasPorLote(loteId)
            remotas.forEach { remota ->
                val filaLocal = q.obtenerLabor(remota.id).executeAsOneOrNull()
                if (filaLocal?.pendiente != 1L) guardarLabor(remota)
            }
        }
    }

    fun guardarLabor(labor: Labor) {
        q.guardarLabor(
            labor.id,
            labor.loteId,
            labor.tipo.name,
            labor.fecha,
            labor.responsable,
            labor.responsableUsuario,
            labor.cantidad,
            labor.cantidadJornales.toLong(),
            labor.observacion,
            labor.fechaRegistro,
            labor.puedeEditar.aEntero(),
            labor.pendiente.aEntero(),
            labor.operacionPendiente?.name,
            labor.eliminado.aEntero()
        )
    }

    fun actualizarLabor(labor: Labor) {
        q.actualizarLabor(
            labor.loteId,
            labor.tipo.name,
            labor.fecha,
            labor.responsable,
            labor.responsableUsuario,
            labor.cantidad,
            labor.cantidadJornales.toLong(),
            labor.observacion,
            labor.fechaRegistro,
            labor.puedeEditar.aEntero(),
            labor.pendiente.aEntero(),
            labor.operacionPendiente?.name,
            labor.eliminado.aEntero(),
            labor.id
        )
    }

    fun reemplazarTemporalPorRemota(idTemporal: Long, remota: Labor) {
        db.transaction {
            q.borrarLabor(idTemporal)
            guardarLabor(remota)
        }
    }

    fun labor(id: Long): Labor? = q.obtenerLabor(id).executeAsOneOrNull()?.aDominio()

    fun pendientes(): List<Labor> = q.obtenerPendientes().executeAsList().map { it.aDominio() }

    fun siguienteIdTemporal(): Long {
        val minimo = q.siguienteIdLocal().executeAsOne()
        return minOf(minimo - 1, -1)
    }

    fun eliminarLabor(id: Long) = q.borrarLabor(id)

    fun guardarUltimaSincronizacion(instante: Long) = q.guardarSincronizacion(instante)

    fun ultimaSincronizacion(): Long? =
        q.obtenerUltimaSincronizacion().executeAsOneOrNull()?.ultima_correcta

    fun guardarSesion(sesion: Sesion) {
        q.guardarSesion(sesion.usuario, sesion.nombre, sesion.rol, sesion.privilegios.joinToString(","))
    }

    fun leerSesion(token: String): Sesion? = q.obtenerSesion().executeAsOneOrNull()?.let {
        Sesion(
            token = token,
            usuario = it.usuario,
            rol = it.rol,
            privilegios = it.privilegios.split(",").filter(String::isNotBlank).toSet(),
            nombre = it.nombre
        )
    }

    fun borrarSesion() = q.borrarSesion()

    private fun LoteFila.aDominio() = Lote(
        id, codigo, nombre, hectareas, cultivo, valle, campaniaCerrada == 1L, dosisMaxima
    )

    private fun LaborFila.aDominio() = Labor(
        id = id,
        loteId = loteId,
        tipo = TipoLabor.valueOf(tipo),
        fecha = fecha,
        responsable = responsable,
        cantidad = cantidad,
        observacion = observacion,
        pendiente = pendiente == 1L,
        responsableUsuario = responsableUsuario,
        cantidadJornales = cantidadJornales.toInt(),
        fechaRegistro = fechaRegistro,
        puedeEditar = puedeEditar == 1L,
        operacionPendiente = operacion?.let(OperacionPendiente::valueOf),
        eliminado = eliminado == 1L
    )

    private fun Boolean.aEntero(): Long = if (this) 1L else 0L
}
