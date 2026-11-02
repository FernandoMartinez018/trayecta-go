package co.trayectago.backend.comun

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun crearDataSource(c: ConfigBaseDatos): HikariDataSource = HikariDataSource(
    HikariConfig().apply {
        jdbcUrl = c.url
        username = c.usuario
        password = c.clave
        maximumPoolSize = 10
        poolName = "trayecta"
    },
)

fun migrar(ds: DataSource) {
    Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate()
}
