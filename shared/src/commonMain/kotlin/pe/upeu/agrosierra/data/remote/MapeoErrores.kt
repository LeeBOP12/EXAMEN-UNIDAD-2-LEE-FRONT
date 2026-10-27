package pe.upeu.agrosierra.data.remote

import pe.upeu.agrosierra.data.remote.dto.ErrorDto
import pe.upeu.agrosierra.domain.model.AgroException

fun traducirError(codigo: Int, error: ErrorDto?): AgroException {
    val mensaje = error?.message ?: when (codigo) {
        400 -> "Revisa los datos enviados"
        401 -> "La sesión expiró"
        403 -> "No tienes permiso para realizar esta operación"
        404 -> "No se encontró el recurso"
        409 -> "La operación contradice una regla del negocio"
        else -> "El servidor no pudo completar la operación"
    }
    return when (codigo) {
        400 -> AgroException.Validacion(mensaje, error?.validationErrors.orEmpty())
        401 -> AgroException.NoAutorizado(mensaje)
        403 -> AgroException.Prohibido(mensaje)
        404 -> AgroException.NoEncontrado(mensaje)
        409 -> AgroException.Conflicto(mensaje)
        else -> AgroException.Servidor(mensaje)
    }
}
