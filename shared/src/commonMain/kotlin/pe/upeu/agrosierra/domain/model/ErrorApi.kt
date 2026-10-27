package pe.upeu.agrosierra.domain.model

data class ErrorApi(
    val status: Int,
    val mensaje: String,
    val validaciones: Map<String, String> = emptyMap()
)

sealed class AgroException(message: String) : Exception(message) {
    class SinConexion(message: String = "Sin conexión. Los cambios quedarán pendientes.") : AgroException(message)
    class TiempoAgotado(message: String = "El servidor tardó demasiado en responder.") : AgroException(message)
    class NoAutorizado(message: String = "La sesión expiró. Inicia sesión nuevamente.") : AgroException(message)
    class Prohibido(message: String) : AgroException(message)
    class NoEncontrado(message: String) : AgroException(message)
    class Validacion(message: String, val campos: Map<String, String>) : AgroException(message)
    class Conflicto(message: String) : AgroException(message)
    class Servidor(message: String) : AgroException(message)
}
