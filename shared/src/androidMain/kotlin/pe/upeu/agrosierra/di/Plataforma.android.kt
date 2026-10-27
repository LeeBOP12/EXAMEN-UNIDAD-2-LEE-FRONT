package pe.upeu.agrosierra.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.DatabaseDriverFactory
import pe.upeu.agrosierra.data.local.MonitorConexionAndroid
import pe.upeu.agrosierra.domain.repository.MonitorConexion
import pe.upeu.agrosierra.data.remote.ApiConfig

actual val platformBaseUrl: String = ApiConfig.BASE_URL_ANDROID

actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { AlmacenSeguro(androidContext()) }
    single<MonitorConexion> { MonitorConexionAndroid(androidContext()) }
}
