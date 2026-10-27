package pe.upeu.agrosierra

import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.agrosierra.domain.model.Lote
import pe.upeu.agrosierra.domain.usecase.FiltrarLotesUseCase

class FiltrarLotesTest {
    @Test
    fun combina_valle_busqueda_y_solo_campanias_abiertas() {
        val lotes = listOf(
            Lote(1, "VAL-001", "Amanecer", 4.0, "Papa", "Valle Azul", false, 2.0),
            Lote(2, "VAL-002", "Cerrado", 3.0, "Papa", "Valle Azul", true, 2.0),
            Lote(3, "SUR-001", "Amanecer Sur", 5.0, "Maíz", "Valle Sur", false, 1.5)
        )

        val resultado = FiltrarLotesUseCase()(
            lotes = lotes,
            busqueda = "amanecer",
            valle = "Valle Azul",
            soloAbiertas = true
        )

        assertEquals(listOf(1L), resultado.map { it.id })
    }
}
