package co.trayectago.backend.usuarios

import co.trayectago.backend.comun.AUTENTICACION_KEYCLOAK
import co.trayectago.backend.comun.sujeto
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Route.rutasUsuarios(repo: RepositorioUsuarios) {
    authenticate(AUTENTICACION_KEYCLOAK) {
        get("/v1/me") {
            val usuario = withContext(Dispatchers.IO) { repo.obtenerOCrear(call.sujeto) }
            call.respond(usuario)
        }
    }
}
