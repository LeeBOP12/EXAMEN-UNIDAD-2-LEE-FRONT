package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.repository.CampoRepositorio

class SincronizarUseCase(private val repositorio: CampoRepositorio) {
    suspend operator fun invoke() = repositorio.sincronizar()
}
