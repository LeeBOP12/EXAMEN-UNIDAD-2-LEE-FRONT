package pe.upeu.agrosierra

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.repository.CampoRepositorioOffline
import pe.upeu.agrosierra.data.repository.Sincronizador
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

class RegistroOfflineTest {
    @Test
    fun registra_en_base_local_sin_invocar_la_red() = runTest {
        val base = BaseLocal(crearBasePrueba())
        base.guardarLote(Lote(3, "SUR-003", "Andenes", 4.0, "Papa", "Valle Sur", false, 2.0))
        val motorSinRed = MockEngine { error("La red no debe invocarse al registrar") }
        val api = AgroApi(HttpClient(motorSinRed)) { }
        val sesiones = SesionFalsa()
        val repositorio = CampoRepositorioOffline(base, api, sesiones, Sincronizador())

        repositorio.registrarLabor(
            loteId = 3,
            tipo = TipoLabor.RIEGO,
            fecha = "2026-10-27",
            cantidad = 12.0,
            observacion = "Turno mañana",
            cantidadJornales = 4
        )

        val guardada = repositorio.observarLabores(3).first().single()
        val loteConPendiente = repositorio.observarLotes().first().single()
        assertTrue(guardada.id < 0)
        assertTrue(guardada.pendiente)
        assertEquals(4, guardada.cantidadJornales)
        assertEquals(1L, loteConPendiente.pendientesSincronizar)
        assertEquals(OperacionPendiente.CREAR, guardada.operacionPendiente)
    }

    private class SesionFalsa : SesionRepositorio {
        private val actual = Sesion(
            token = "token", usuario = "tecnico1", rol = "TECNICO",
            privilegios = setOf(Privilegios.LABOR_CREAR)
        )
        override val sesion: Flow<Sesion?> = MutableStateFlow(actual)
        override suspend fun iniciar(usuario: String, clave: String) = actual
        override suspend fun cerrar() = Unit
        override suspend fun sesionActual(): Sesion = actual
    }
}
