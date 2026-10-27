package pe.upeu.agrosierra

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.upeu.agrosierra.AgroDb
import kotlin.random.Random

actual fun crearBasePrueba(): AgroDb =
    AgroDb(NativeSqliteDriver(AgroDb.Schema, "agrocampo-test-${Random.nextLong()}.db"))
