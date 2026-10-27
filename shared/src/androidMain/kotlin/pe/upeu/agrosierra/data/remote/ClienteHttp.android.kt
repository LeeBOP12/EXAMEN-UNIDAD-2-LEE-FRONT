package pe.upeu.agrosierra.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun motorHttp(): HttpClientEngine = Android.create()
