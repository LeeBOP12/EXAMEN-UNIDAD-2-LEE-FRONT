package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.repository.SesionRepositorio

class CerrarSesionUseCase(private val repositorio: SesionRepositorio) {
    suspend operator fun invoke() = repositorio.cerrar()
}
