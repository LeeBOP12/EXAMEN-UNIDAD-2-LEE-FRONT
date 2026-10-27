package pe.upeu.agrosierra.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.usecase.RegistrarLaborUseCase
import pe.upeu.agrosierra.domain.repository.CampoRepositorio
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

data class DetalleUiState(
    val cargando: Boolean = true,
    val lote: Lote? = null,
    val labores: List<Labor> = emptyList(),
    val sesion: Sesion? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val erroresCampos: Map<String, String> = emptyMap(),
    val cerrarFormulario: Boolean = false,
    val recursoNoEncontrado: Boolean = false
)

class DetalleViewModel(
    private val loteId: Long,
    private val repositorio: CampoRepositorio,
    private val sesiones: SesionRepositorio,
    private val registrarLabor: RegistrarLaborUseCase
) : ViewModel() {
    private val proceso = MutableStateFlow(DetalleUiState())

    val ui: StateFlow<DetalleUiState> = combine(
        repositorio.observarLabores(loteId), sesiones.sesion, proceso
    ) { labores, sesion, actual ->
        actual.copy(labores = labores, sesion = sesion)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DetalleUiState()
    )

    init {
        viewModelScope.launch {
            proceso.value = proceso.value.copy(
                lote = repositorio.lote(loteId),
                cargando = true
            )
            try {
                repositorio.refrescarLabores(loteId)
                proceso.value = proceso.value.copy(
                    lote = repositorio.lote(loteId),
                    cargando = false
                )
            } catch (error: AgroException.NoEncontrado) {
                runCatching { repositorio.refrescarLotes() }
                proceso.value = proceso.value.copy(
                    cargando = false,
                    recursoNoEncontrado = true,
                    error = error.message ?: "El lote ya no existe; se actualizó el padrón"
                )
            } catch (error: Exception) {
                proceso.value = proceso.value.copy(
                    cargando = false,
                    error = error.message
                )
            }
        }
    }

    fun prepararFormulario() {
        proceso.value = proceso.value.copy(
            erroresCampos = emptyMap(),
            error = null,
            cerrarFormulario = false
        )
    }

    fun registrar(
        tipo: TipoLabor,
        fecha: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int
    ) = ejecutar(
        mensaje = "Labor guardada localmente; sincroniza para enviarla",
        cerrarFormulario = true
    ) {
        registrarLabor(
            loteId, tipo, fecha, cantidad, observacion, cantidadJornales
        )
    }

    fun actualizar(
        labor: Labor,
        tipo: TipoLabor,
        fecha: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int
    ) = ejecutar(
        mensaje = "Corrección guardada localmente",
        cerrarFormulario = true
    ) {
        repositorio.actualizarLabor(
            labor.copy(
                tipo = tipo,
                fecha = fecha,
                cantidad = cantidad,
                cantidadJornales = cantidadJornales,
                observacion = observacion
            )
        )
    }

    fun eliminar(labor: Labor) = ejecutar(
        "Eliminación pendiente de sincronización"
    ) {
        repositorio.eliminarLabor(labor)
    }

    fun cerrarCampania() = ejecutar("Campaña cerrada") {
        repositorio.cerrarCampania(loteId)
        proceso.value = proceso.value.copy(lote = repositorio.lote(loteId))
    }

    fun sincronizar() = ejecutar("Sincronización completada") {
        val resumen = repositorio.sincronizar()
        resumen.error?.let { throw it }
        proceso.value = proceso.value.copy(lote = repositorio.lote(loteId))
    }

    fun formularioCerrado() {
        proceso.value = proceso.value.copy(
            cerrarFormulario = false,
            erroresCampos = emptyMap()
        )
    }

    fun limpiarAviso() {
        proceso.value = proceso.value.copy(
            mensaje = null,
            error = null,
            erroresCampos = emptyMap()
        )
    }

    private fun ejecutar(
        mensaje: String,
        cerrarFormulario: Boolean = false,
        bloque: suspend () -> Unit
    ) {
        if (proceso.value.procesando) return
        viewModelScope.launch {
            proceso.value = proceso.value.copy(
                procesando = true,
                error = null,
                mensaje = null,
                erroresCampos = emptyMap()
            )
            try {
                bloque()
                proceso.value = proceso.value.copy(
                    procesando = false,
                    mensaje = mensaje,
                    cerrarFormulario = cerrarFormulario
                )
            } catch (error: AgroException.Validacion) {
                proceso.value = proceso.value.copy(
                    procesando = false,
                    error = error.message,
                    erroresCampos = error.campos
                )
            } catch (error: Exception) {
                proceso.value = proceso.value.copy(
                    procesando = false,
                    error = error.message ?: "Operación fallida"
                )
            }
        }
    }
}
