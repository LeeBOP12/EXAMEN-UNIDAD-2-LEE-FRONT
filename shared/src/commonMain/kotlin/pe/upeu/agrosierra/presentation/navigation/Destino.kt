package pe.upeu.agrosierra.presentation.navigation

sealed interface Destino {
    data object Lotes : Destino
    data object Perfil : Destino
    data class Detalle(val loteId: Long) : Destino
}
