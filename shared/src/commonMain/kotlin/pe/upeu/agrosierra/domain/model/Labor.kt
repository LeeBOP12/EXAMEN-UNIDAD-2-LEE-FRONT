package pe.upeu.agrosierra.domain.model

data class Labor(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val cantidad: Double,
    val observacion: String,
    val pendiente: Boolean,
    val responsableUsuario: String = responsable,
    val cantidadJornales: Int = 1,
    val fechaRegistro: String = "",
    val puedeEditar: Boolean = false,
    val operacionPendiente: OperacionPendiente? = null,
    val eliminado: Boolean = false
)

enum class OperacionPendiente { CREAR, ACTUALIZAR, ELIMINAR }
