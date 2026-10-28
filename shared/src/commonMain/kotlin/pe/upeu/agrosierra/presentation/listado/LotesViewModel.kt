package pe.upeu.agrosierra.presentation.listado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.model.Lote
import pe.upeu.agrosierra.domain.usecase.FiltrarLotesUseCase
import pe.upeu.agrosierra.domain.usecase.SincronizarUseCase
import pe.upeu.agrosierra.domain.repository.CampoRepositorio
import pe.upeu.agrosierra.domain.repository.MonitorConexion

data class LotesUiState(
    val cargando: Boolean = true,
    val sincronizando: Boolean = false,
    val lotes: List<Lote> = emptyList(),
    val totalLotesLocales: Int = 0,
    val vallesDisponibles: List<String> = emptyList(),
    val busqueda: String = "",
    val valle: String = "",
    val soloAbiertas: Boolean = false,
    val ultimaSincronizacion: Long? = null,
    val resumenSincronizacion: String? = null,
    val error: String? = null
) {
    val vacio get() = !cargando && totalLotesLocales == 0 && error == null
    val sinResultados get() =
        !cargando && totalLotesLocales > 0 && lotes.isEmpty() && error == null
}

private data class Filtros(
    val busqueda: String,
    val valle: String,
    val soloAbiertas: Boolean
)

class LotesViewModel(
    private val repositorio: CampoRepositorio,
    private val sincronizarUseCase: SincronizarUseCase,
    private val monitorConexion: MonitorConexion,
    private val filtrarLotes: FiltrarLotesUseCase
) : ViewModel() {
    private val busqueda = MutableStateFlow("")
    private val valle = MutableStateFlow("")
    private val soloAbiertas = MutableStateFlow(false)
    private val estadoProceso = MutableStateFlow(LotesUiState())
    private val filtros = combine(busqueda, valle, soloAbiertas, ::Filtros)

    val ui: StateFlow<LotesUiState> = combine(
        repositorio.observarLotes(), filtros, estadoProceso
    ) { todos, filtro, proceso ->
        proceso.copy(
            cargando = false,
            lotes = filtrarLotes(
                todos, filtro.busqueda, filtro.valle, filtro.soloAbiertas
            ),
            totalLotesLocales = todos.size,
            vallesDisponibles = todos.map(Lote::valle).distinct().sorted(),
            busqueda = filtro.busqueda,
            valle = filtro.valle,
            soloAbiertas = filtro.soloAbiertas
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LotesUiState()
    )

    init {
        viewModelScope.launch {
            estadoProceso.value = estadoProceso.value.copy(
                ultimaSincronizacion = repositorio.ultimaSincronizacion()
            )
            sincronizar()
        }
        viewModelScope.launch {
            monitorConexion.conectada.drop(1).filter { it }.collect { sincronizar() }
        }
    }

    fun buscar(valor: String) { busqueda.value = valor }
    fun filtrarValle(valor: String) { valle.value = valor }
    fun alternarSoloAbiertas() { soloAbiertas.value = !soloAbiertas.value }

    fun sincronizar() {
        if (estadoProceso.value.sincronizando) return
        viewModelScope.launch {
            estadoProceso.value = estadoProceso.value.copy(
                sincronizando = true,
                error = null
            )
            runCatching { sincronizarUseCase() }
                .onSuccess {
                    estadoProceso.value = estadoProceso.value.copy(
                        sincronizando = false,
                        ultimaSincronizacion = it.ultimaSincronizacion,
                        resumenSincronizacion =
                            "Enviados: " + it.enviados +
                            " · recibidos: " + it.descargados +
                            " · errores: " + it.errores,
                        error = it.error?.message
                    )
                }
                .onFailure {
                    estadoProceso.value = estadoProceso.value.copy(
                        sincronizando = false,
                        error = it.message
                            ?: "No se pudo sincronizar; se muestran los datos locales"
                    )
                }
        }
    }

    fun limpiarError() {
        estadoProceso.value = estadoProceso.value.copy(error = null)
    }
}
