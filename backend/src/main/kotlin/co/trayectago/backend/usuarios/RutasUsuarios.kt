package co.trayectago.backend.usuarios

import co.trayectago.backend.comun.AUTENTICACION_KEYCLOAK
import co.trayectago.backend.comun.sujeto
import co.trayectago.backend.comun.responderProblema
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import kotlinx.coroutines.withContext

fun Route.rutasUsuarios(repo: RepositorioUsuarios) {
    authenticate(AUTENTICACION_KEYCLOAK) {
        get("/v1/me") {
            val usuario = withContext(Dispatchers.IO) { repo.obtenerOCrear(call.sujeto) }
            call.respond(usuario)
        }
        get("/v1/me/vehiculo") {
            val usuario = withContext(Dispatchers.IO) { repo.obtenerOCrear(call.sujeto) }
            val vehiculo = withContext(Dispatchers.IO) { repo.obtenerVehiculo(UUID.fromString(usuario.id)) }
            if (vehiculo == null) {
                call.responderProblema(HttpStatusCode.NotFound, "No encontrado")
            } else {
                call.respond(vehiculo)
            }
        }
        put("/v1/me/vehiculo") {
            val entrada = call.receive<VehiculoEntrada>()
            val vehiculo = validarVehiculo(entrada)
            val usuario = withContext(Dispatchers.IO) { repo.obtenerOCrear(call.sujeto) }
            val guardado = withContext(Dispatchers.IO) { repo.guardarVehiculo(UUID.fromString(usuario.id), vehiculo) }
            call.respond(guardado)
        }
    }
}
