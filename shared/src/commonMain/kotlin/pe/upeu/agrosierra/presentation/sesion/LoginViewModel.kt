package pe.upeu.agrosierra.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.usecase.IniciarSesionUseCase

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val iniciarSesion: IniciarSesionUseCase) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui = _ui.asStateFlow()

    fun usuario(valor: String) { _ui.value = _ui.value.copy(usuario = valor, error = null) }
    fun clave(valor: String) { _ui.value = _ui.value.copy(clave = valor, error = null) }

    fun ingresar() {
        val actual = _ui.value
        viewModelScope.launch {
            _ui.value = actual.copy(cargando = true, error = null)
            runCatching { iniciarSesion(actual.usuario, actual.clave) }
                .onSuccess { _ui.value = _ui.value.copy(cargando = false) }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _ui.value = _ui.value.copy(
                        cargando = false,
                        error = error.message ?: "No se pudo ingresar"
                    )
                }
        }
    }
}
