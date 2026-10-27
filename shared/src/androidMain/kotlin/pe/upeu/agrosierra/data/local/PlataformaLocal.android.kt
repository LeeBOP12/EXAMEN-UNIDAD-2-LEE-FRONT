package pe.upeu.agrosierra.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.upeu.agrosierra.AgroDb
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(AgroDb.Schema, context, "agrocampo.db")
}

actual class AlmacenSeguro(private val context: Context) {
    private val preferencias by lazy {
        val clave = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "agrocampo_seguro",
            clave,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual fun guardarToken(token: String) {
        preferencias.edit().putString("token", token).apply()
    }

    actual fun leerToken(): String? = preferencias.getString("token", null)

    actual fun borrarToken() {
        preferencias.edit().remove("token").apply()
    }
}

actual fun ahoraMillis(): Long = System.currentTimeMillis()

actual fun formatearInstante(instante: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(instante))
