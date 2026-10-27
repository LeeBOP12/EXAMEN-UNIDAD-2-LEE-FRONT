package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.model.TipoLabor
import pe.upeu.agrosierra.domain.repository.CampoRepositorio

class RegistrarLaborUseCase(private val repositorio: CampoRepositorio) {
    suspend operator fun invoke(
        loteId: Long,
        tipo: TipoLabor,
        fecha: String,
        cantidad: Double,
        observacion: String,
        cantidadJornales: Int = 1
    ) {
        require(fecha.isNotBlank()) { "La fecha es obligatoria" }
        require(cantidad > 0.0) { "La cantidad debe ser mayor que cero" }
        require(cantidadJornales > 0) { "La cantidad de jornales debe ser mayor que cero" }
        repositorio.registrarLabor(
            loteId, tipo, fecha, cantidad, observacion, cantidadJornales
        )
    }
}
