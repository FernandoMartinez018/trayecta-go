package co.trayectago.backend

import co.trayectago.backend.comun.Configuracion
import co.trayectago.backend.comun.crearDataSource
import co.trayectago.backend.comun.instalarObservabilidad
import co.trayectago.backend.comun.instalarProblemas
import co.trayectago.backend.comun.jsonTrayecta
import co.trayectago.backend.comun.migrar
import co.trayectago.backend.comun.rutaSalud
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import javax.sql.DataSource

fun main() {
    val config = Configuracion.desdeEntorno(System.getenv())
    val dataSource = crearDataSource(config.db)
    migrar(dataSource)
    embeddedServer(Netty, port = config.puerto, host = "0.0.0.0") {
        moduloTrayecta(config, dataSource)
    }.start(wait = true)
}

fun Application.moduloTrayecta(config: Configuracion, dataSource: DataSource) {
    install(ContentNegotiation) { json(jsonTrayecta) }
    instalarObservabilidad()
    instalarProblemas()
    routing {
        rutaSalud(dataSource, config.version)
    }
}
