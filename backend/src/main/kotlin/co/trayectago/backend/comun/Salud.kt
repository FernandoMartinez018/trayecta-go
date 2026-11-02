package co.trayectago.backend.comun

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.sql.DataSource

@Serializable
data class Salud(val estado: String, val version: String)

fun Route.rutaSalud(dataSource: DataSource, version: String) {
    get("/v1/salud") {
        val baseResponde = runCatching {
            withContext(Dispatchers.IO) { dataSource.connection.use { it.isValid(2) } }
        }.getOrDefault(false)
        call.respond(Salud(if (baseResponde) "ok" else "degradado", version))
    }
}
