package pe.upeu.agrosierra.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.BaseLocal
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.domain.model.AgroException
import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

class SesionRepositorioImpl(
    private val api: AgroApi,
    private val local: BaseLocal,
    private val seguro: AlmacenSeguro
) : SesionRepositorio {
    private val estado = MutableStateFlow(seguro.leerToken()?.let(local::leerSesion))
    override val sesion = estado.asStateFlow()

    init {
        if (estado.value == null) seguro.borrarToken()
    }

    override suspend fun iniciar(usuario: String, clave: String): Sesion {
        val nueva = api.login(usuario, clave).aDominio()
        seguro.guardarToken(nueva.token)
        local.guardarSesion(nueva)
        estado.value = nueva
        return nueva
    }

    override suspend fun cerrar() {
        seguro.borrarToken()
        local.borrarSesion()
        estado.value = null
    }

    override suspend fun sesionActual(): Sesion? = estado.value

    override suspend fun validarGuardada() {
        val actual = estado.value ?: return
        try {
            val usuario = api.usuarioActual()
            val validada = Sesion(
                token = actual.token,
                usuario = usuario.usuario,
                rol = usuario.rol,
                privilegios = usuario.privilegios.toSet(),
                nombre = usuario.nombre
            )
            local.guardarSesion(validada)
            estado.value = validada
        } catch (_: AgroException.NoAutorizado) {
            cerrar()
        } catch (_: AgroException.SinConexion) {
            // Sin red se conserva la sesión local para mantener el funcionamiento offline.
        } catch (_: AgroException.TiempoAgotado) {
            // Un timeout no invalida un token que todavía puede ser correcto.
        } catch (_: AgroException) {
            // Un fallo de servidor tampoco invalida la sesión local.
        } catch (_: Exception) {
            // Una respuesta inesperada se reintentará en la próxima apertura.
        }
    }
}
