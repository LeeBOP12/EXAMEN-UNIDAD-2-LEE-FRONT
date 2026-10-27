package pe.upeu.agrosierra.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.*

@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)

@Serializable
data class ErrorDto(
    val status: Int,
    val error: String = "",
    val message: String,
    val path: String = "",
    val validationErrors: Map<String, String>? = null
)

@Serializable data class LoginRequestDto(val username: String, val password: String)
@Serializable
data class LoginResponseDto(
    val token: String,
    val expiraEnSegundos: Long,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val privilegios: List<String>
) {
    fun aDominio() = Sesion(
        token = token, usuario = usuario, rol = rol,
        privilegios = privilegios.toSet(), nombre = nombre
    )
}

@Serializable
data class UsuarioActualDto(
    val usuario: String,
    val nombre: String,
    val rol: String,
    val privilegios: List<String>
)

@Serializable
data class LoteDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val hectareas: Double,
    val cultivo: String,
    val valle: String,
    val campaniaCerrada: Boolean,
    val dosisMaximaPorHectarea: Double
) {
    fun aDominio() = Lote(id, codigo, nombre, hectareas, cultivo, valle, campaniaCerrada, dosisMaximaPorHectarea)
}

@Serializable
data class LaborDto(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val responsableUsuario: String = "",
    val cantidad: Double,
    val cantidadJornales: Int = 1,
    val observacion: String,
    val fechaRegistro: String = "",
    val puedeEditar: Boolean = false
) {
    fun aDominio() = Labor(
        id = id,
        loteId = loteId,
        tipo = tipo,
        fecha = fecha,
        responsable = responsable,
        cantidad = cantidad,
        observacion = observacion,
        pendiente = false,
        responsableUsuario = responsableUsuario,
        cantidadJornales = cantidadJornales,
        fechaRegistro = fechaRegistro,
        puedeEditar = puedeEditar
    )
}

@Serializable
data class LaborRequestDto(
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val cantidad: Double,
    val cantidadJornales: Int,
    val observacion: String
)
