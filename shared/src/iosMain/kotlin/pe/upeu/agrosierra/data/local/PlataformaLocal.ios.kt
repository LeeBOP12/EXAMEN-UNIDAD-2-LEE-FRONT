package pe.upeu.agrosierra.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.upeu.agrosierra.AgroDb
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AgroDb.Schema, "agrocampo.db")
}

actual class AlmacenSeguro {
    private val preferencias = NSUserDefaults.standardUserDefaults
    actual fun guardarToken(token: String) = preferencias.setObject(token, forKey = "agrocampo_token")
    actual fun leerToken(): String? = preferencias.stringForKey("agrocampo_token")
    actual fun borrarToken() = preferencias.removeObjectForKey("agrocampo_token")
}

actual fun ahoraMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun formatearInstante(instante: Long): String {
    val formato = NSDateFormatter().apply { dateFormat = "dd/MM/yyyy HH:mm" }
    return formato.stringFromDate(NSDate(timeIntervalSince1970 = instante / 1000.0))
}
