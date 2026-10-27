package pe.upeu.agrosierra

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pe.upeu.agrosierra.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@MainApplication) }
    }
}
