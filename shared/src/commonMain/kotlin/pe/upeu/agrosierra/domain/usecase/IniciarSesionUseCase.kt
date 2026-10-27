package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

class IniciarSesionUseCase(private val repositorio: SesionRepositorio) {
    suspend operator fun invoke(usuario: String, clave: String): Sesion {
        require(usuario.isNotBlank()) { "Ingresa tu usuario" }
        require(clave.isNotBlank()) { "Ingresa tu contraseña" }
        return repositorio.iniciar(usuario.trim(), clave)
    }
}
