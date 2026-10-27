package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.model.Lote

class FiltrarLotesUseCase {
    operator fun invoke(
        lotes: List<Lote>,
        busqueda: String,
        valle: String,
        soloAbiertas: Boolean
    ): List<Lote> = lotes.filter { lote ->
        val coincideBusqueda = busqueda.isBlank() ||
            lote.codigo.contains(busqueda, ignoreCase = true) ||
            lote.nombre.contains(busqueda, ignoreCase = true) ||
            lote.cultivo.contains(busqueda, ignoreCase = true)
        val coincideValle = valle.isBlank() || lote.valle == valle
        val coincideEstado = !soloAbiertas || !lote.campaniaCerrada
        coincideBusqueda && coincideValle && coincideEstado
    }
}
