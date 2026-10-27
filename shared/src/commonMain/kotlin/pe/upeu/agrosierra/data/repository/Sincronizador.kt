package pe.upeu.agrosierra.data.repository

class Sincronizador {
    suspend fun ejecutar(
        enviarPendientes: suspend () -> Int,
        descargarServidor: suspend () -> Int
    ): Pair<Int, Int> {
        val enviados = enviarPendientes()
        val descargados = descargarServidor()
        return enviados to descargados
    }
}
