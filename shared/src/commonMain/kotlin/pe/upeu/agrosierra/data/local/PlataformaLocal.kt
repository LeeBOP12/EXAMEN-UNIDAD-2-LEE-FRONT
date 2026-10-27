package pe.upeu.agrosierra.data.local

import app.cash.sqldelight.db.SqlDriver
import pe.upeu.agrosierra.AgroDb

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect class AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}

fun crearBase(factory: DatabaseDriverFactory) = AgroDb(factory.createDriver())

expect fun ahoraMillis(): Long
expect fun formatearInstante(instante: Long): String
