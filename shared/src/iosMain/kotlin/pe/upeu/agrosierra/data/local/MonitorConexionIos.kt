package pe.upeu.agrosierra.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.upeu.agrosierra.domain.repository.MonitorConexion

class MonitorConexionIos : MonitorConexion {
    override val conectada: Flow<Boolean> = flowOf(true)
}
