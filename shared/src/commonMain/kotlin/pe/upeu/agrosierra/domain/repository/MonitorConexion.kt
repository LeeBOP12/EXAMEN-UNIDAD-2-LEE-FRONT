package pe.upeu.agrosierra.domain.repository

import kotlinx.coroutines.flow.Flow

interface MonitorConexion {
    val conectada: Flow<Boolean>
}
