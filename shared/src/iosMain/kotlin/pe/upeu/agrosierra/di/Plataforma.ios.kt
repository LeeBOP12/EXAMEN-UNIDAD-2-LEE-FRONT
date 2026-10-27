package pe.upeu.agrosierra.di

import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.DatabaseDriverFactory
import pe.upeu.agrosierra.data.local.MonitorConexionIos
import pe.upeu.agrosierra.domain.repository.MonitorConexion
import pe.upeu.agrosierra.data.remote.ApiConfig

actual val platformBaseUrl: String = ApiConfig.BASE_URL_IOS

actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { AlmacenSeguro() }
    single<MonitorConexion> { MonitorConexionIos() }
}
