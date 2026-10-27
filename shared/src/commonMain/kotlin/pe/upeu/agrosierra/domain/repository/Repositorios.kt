package pe.upeu.agrosierra.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.upeu.agrosierra.domain.model.*

interface SesionRepositorio {
    val sesion: Flow<Sesion?>
    suspend fun iniciar(usuario: String, clave: String): Sesion
    suspend fun cerrar()
    suspend fun sesionActual(): Sesion?
    suspend fun validarGuardada() {}
}

interface CampoRepositorio {
    fun observarLotes(): Flow<List<Lote>>
    fun observarLabores(loteId: Long): Flow<List<Labor>>
    suspend fun lote(id: Long): Lote?
    suspend fun refrescarLotes(valle: String? = null): Int
    suspend fun refrescarLabores(loteId: Long): Int
    suspend fun registrarLabor(
        loteId: Long,
        tipo: TipoLabor,
        fecha: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int = 1
    )
    suspend fun actualizarLabor(labor: Labor)
    suspend fun eliminarLabor(labor: Labor)
    suspend fun cerrarCampania(loteId: Long)
    suspend fun sincronizar(): ResumenSincronizacion
    suspend fun ultimaSincronizacion(): Long?
}
