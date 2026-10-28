package pe.upeu.agrosierra.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import pe.upeu.agrosierra.data.remote.dto.*
import pe.upeu.agrosierra.domain.model.AgroException
import pe.upeu.agrosierra.domain.model.Labor

class AgroApi(
    private val cliente: HttpClient,
    private val sesionExpirada: suspend () -> Unit
) {
    suspend fun login(usuario: String, clave: String): LoginResponseDto =
        ejecutar { cliente.post("auth/login") { setBody(LoginRequestDto(usuario, clave)) } }

    suspend fun usuarioActual(): UsuarioActualDto =
        ejecutar { cliente.get("usuarios/me") }

    suspend fun lotes(valle: String? = null): List<LoteDto> {
        val acumulado = mutableListOf<LoteDto>()
        var pagina = 0
        do {
            val respuesta: PaginaDto<LoteDto> = ejecutar {
                cliente.get("lotes") {
                    parameter("pagina", pagina)
                    parameter("tamanio", 50)
                    valle?.takeIf { it.isNotBlank() }?.let { parameter("valle", it) }
                }
            }
            acumulado += respuesta.contenido
            if (respuesta.contenido.isEmpty()) break
            pagina++
        } while (!respuesta.ultima)
        return acumulado
    }

    suspend fun labores(loteId: Long): List<LaborDto> {
        val acumulado = mutableListOf<LaborDto>()
        var pagina = 0
        do {
            val respuesta: PaginaDto<LaborDto> = ejecutar {
                cliente.get("lotes/$loteId/labores") {
                    parameter("pagina", pagina)
                    parameter("tamanio", 50)
                }
            }
            acumulado += respuesta.contenido
            if (respuesta.contenido.isEmpty()) break
            pagina++
        } while (!respuesta.ultima)
        return acumulado
    }

    suspend fun crearLabor(labor: Labor): LaborDto = ejecutar {
        cliente.post("labores") {
            setBody(labor.aRequest())
        }
    }

    suspend fun actualizarLabor(labor: Labor): LaborDto = ejecutar {
        cliente.put("labores/${labor.id}") {
            setBody(labor.aRequest())
        }
    }

    suspend fun eliminarLabor(id: Long) {
        ejecutarSinContenido { cliente.delete("labores/$id") }
    }

    suspend fun cerrarCampania(loteId: Long): LoteDto =
        ejecutar { cliente.post("lotes/$loteId/cerrar-campania") }


    private suspend inline fun <reified T> ejecutar(peticion: suspend () -> HttpResponse): T {
        val response = try {
            peticion()
        } catch (_: HttpRequestTimeoutException) {
            throw AgroException.TiempoAgotado()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AgroException.SinConexion()
        }
        if (response.status.isSuccess()) return response.body()
        val error = runCatching { response.body<ErrorDto>() }.getOrNull()
        if (response.status == HttpStatusCode.Unauthorized) sesionExpirada()
        throw traducirError(response.status.value, error)
    }

    private suspend fun ejecutarSinContenido(peticion: suspend () -> HttpResponse) {
        val response = try {
            peticion()
        } catch (_: HttpRequestTimeoutException) {
            throw AgroException.TiempoAgotado()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AgroException.SinConexion()
        }
        if (response.status.isSuccess()) return
        val error = runCatching { response.body<ErrorDto>() }.getOrNull()
        if (response.status == HttpStatusCode.Unauthorized) sesionExpirada()
        throw traducirError(response.status.value, error)
    }

    private fun Labor.aRequest() =
        LaborRequestDto(loteId, tipo, fecha, cantidad, cantidadJornales, observacion)
}
