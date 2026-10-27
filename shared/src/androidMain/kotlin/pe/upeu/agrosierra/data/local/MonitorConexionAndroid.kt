package pe.upeu.agrosierra.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import pe.upeu.agrosierra.domain.repository.MonitorConexion

class MonitorConexionAndroid(context: Context) : MonitorConexion {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val conectada: Flow<Boolean> = callbackFlow {
        fun estadoActual(): Boolean {
            val red = manager.activeNetwork ?: return false
            val capacidades = manager.getNetworkCapabilities(red) ?: return false
            return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        trySend(estadoActual())
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(estadoActual()) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
