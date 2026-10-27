package pe.upeu.agrosierra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import pe.upeu.agrosierra.domain.repository.SesionRepositorio
import pe.upeu.agrosierra.presentation.detalle.DetalleScreen
import pe.upeu.agrosierra.presentation.listado.LotesScreen
import pe.upeu.agrosierra.presentation.perfil.PerfilScreen
import pe.upeu.agrosierra.presentation.sesion.LoginScreen
import pe.upeu.agrosierra.presentation.theme.AgroTema
import pe.upeu.agrosierra.presentation.navigation.Destino

@Composable
fun App() {
    AgroTema {
        val sesiones = koinInject<SesionRepositorio>()
        val sesion by sesiones.sesion.collectAsState(null)
        var verificandoSesion by remember { mutableStateOf(true) }
        var destino by remember { mutableStateOf<Destino>(Destino.Lotes) }

        LaunchedEffect(Unit) {
            try {
                sesiones.validarGuardada()
            } finally {
                verificandoSesion = false
            }
        }

        when {
            verificandoSesion -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            sesion == null -> LoginScreen()
            else -> when (val pantalla = destino) {
                Destino.Lotes -> LotesScreen(
                    abrirLote = { destino = Destino.Detalle(it) },
                    abrirPerfil = { destino = Destino.Perfil }
                )
                Destino.Perfil -> PerfilScreen { destino = Destino.Lotes }
                is Destino.Detalle -> DetalleScreen(pantalla.loteId) {
                    destino = Destino.Lotes
                }
            }
        }
    }
}
