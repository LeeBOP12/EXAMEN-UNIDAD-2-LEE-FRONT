package pe.upeu.agrosierra.data.remote

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

expect fun motorHttp(): HttpClientEngine

fun crearClienteHttp(
    baseUrl: String,
    tokenProvider: () -> String?,
    engine: HttpClientEngine = motorHttp()
): HttpClient = HttpClient(engine) {
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.NONE
        sanitizeHeader { it == HttpHeaders.Authorization }
    }
    defaultRequest {
        url(baseUrl)
        tokenProvider()?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
}
