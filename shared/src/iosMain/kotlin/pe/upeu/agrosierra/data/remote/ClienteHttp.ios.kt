package pe.upeu.agrosierra.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun motorHttp(): HttpClientEngine = Darwin.create()
