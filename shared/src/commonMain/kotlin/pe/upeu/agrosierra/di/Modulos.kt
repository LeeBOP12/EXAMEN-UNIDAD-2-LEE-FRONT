package pe.upeu.agrosierra.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.*
import pe.upeu.agrosierra.data.remote.*
import pe.upeu.agrosierra.data.repository.*
import pe.upeu.agrosierra.domain.usecase.*
import pe.upeu.agrosierra.domain.repository.*
import pe.upeu.agrosierra.presentation.detalle.DetalleViewModel
import pe.upeu.agrosierra.presentation.listado.LotesViewModel
import pe.upeu.agrosierra.presentation.sesion.LoginViewModel

expect val platformModule: org.koin.core.module.Module
expect val platformBaseUrl: String

private val comun = module {
    single { crearBase(get()) }
    single { BaseLocal(get()) }
    single {
        val seguro: AlmacenSeguro = get()
        crearClienteHttp(
            baseUrl = platformBaseUrl,
            tokenProvider = { seguro.leerToken() }
        )
    }
    single {
        AgroApi(
            cliente = get(),
            sesionExpirada = { get<SesionRepositorio>().cerrar() }
        )
    }
    single<SesionRepositorio> { SesionRepositorioImpl(get(), get(), get()) }
    single { Sincronizador() }
    single<CampoRepositorio> { CampoRepositorioOffline(get(), get(), get(), get()) }
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { RegistrarLaborUseCase(get()) }
    factory { SincronizarUseCase(get()) }
    factory { FiltrarLotesUseCase() }
    factory { LoginViewModel(get()) }
    factory { LotesViewModel(get(), get(), get(), get()) }
    factory { params -> DetalleViewModel(params.get(), get(), get(), get()) }
}

fun initKoin(config: KoinApplication.() -> Unit = {}) =
    startKoin {
        config()
        modules(comun, platformModule)
    }
