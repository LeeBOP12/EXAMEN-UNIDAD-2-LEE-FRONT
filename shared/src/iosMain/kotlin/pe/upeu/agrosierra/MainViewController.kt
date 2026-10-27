package pe.upeu.agrosierra

import androidx.compose.ui.window.ComposeUIViewController
import pe.upeu.agrosierra.di.initKoin

fun MainViewController() = ComposeUIViewController {
    App()
}

fun iniciarDependencias() {
    initKoin()
}
