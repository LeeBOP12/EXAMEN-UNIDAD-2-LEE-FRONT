package pe.upeu.agrosierra

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.upeu.agrosierra.AgroDb

actual fun crearBasePrueba(): AgroDb {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AgroDb.Schema.create(driver)
    return AgroDb(driver)
}
