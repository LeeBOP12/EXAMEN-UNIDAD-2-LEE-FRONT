package pe.upeu.agrosierra

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.agrosierra.data.repository.Sincronizador

class OrdenSincronizacionTest {
    @Test
    fun envia_pendientes_antes_de_descargar_servidor() = runTest {
        val eventos = mutableListOf<String>()

        val resultado = Sincronizador().ejecutar(
            enviarPendientes = { eventos += "enviar"; 2 },
            descargarServidor = { eventos += "descargar"; 9 }
        )

        assertEquals(listOf("enviar", "descargar"), eventos)
        assertEquals(2 to 9, resultado)
    }
}
