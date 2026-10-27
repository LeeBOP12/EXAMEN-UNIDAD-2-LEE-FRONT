package pe.upeu.agrosierra.domain.model

data class ResumenSincronizacion(
    val enviados: Int,
    val descargados: Int,
    val errores: Int,
    val ultimaSincronizacion: Long,
    val error: AgroException? = null
)
