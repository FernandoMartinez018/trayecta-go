package co.trayectago.backend.comun

import com.auth0.jwk.JwkException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/** JSON de la API: ignora campos desconocidos, omite nulos y siempre incluye los valores por defecto. */
val jsonTrayecta = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

/** Problem Details (RFC 9457), como lo define el contrato. */
@Serializable
data class Problema(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
)

class SolicitudInvalida(detalle: String) : RuntimeException(detalle)

private val PROBLEM_JSON = ContentType("application", "problem+json")

suspend fun ApplicationCall.responderProblema(estado: HttpStatusCode, titulo: String, detalle: String? = null) {
    respondText(
        jsonTrayecta.encodeToString(Problema.serializer(), Problema(title = titulo, status = estado.value, detail = detalle)),
        PROBLEM_JSON,
        estado,
    )
}

fun Application.instalarProblemas() {
    install(StatusPages) {
        exception<SolicitudInvalida> { call, e ->
            call.responderProblema(HttpStatusCode.BadRequest, "Solicitud inválida", e.message)
        }
        exception<BadRequestException> { call, _ ->
            call.responderProblema(HttpStatusCode.BadRequest, "Solicitud inválida", "El cuerpo no es un JSON válido para este recurso.")
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.responderProblema(HttpStatusCode.NotFound, "No encontrado")
        }
        // Ktor 3.6 envuelve en IOException la falla del JWKS; un 401 haría que la app cierre la sesión por una caída de Keycloak.
        exception<IOException> { call, e ->
            // Se busca en toda la cadena: la recuperación de trazas de las corrutinas puede envolver la excepción otra vez.
            val falloJwks = generateSequence<Throwable>(e) { it.cause }.firstOrNull { it is JwkException }
            if (falloJwks != null) {
                call.application.log.warn("No se pudo obtener la llave de firma: {}", falloJwks::class.simpleName)
                call.responderProblema(HttpStatusCode.ServiceUnavailable, "Servicio no disponible", "No pudimos validar tu sesión. Intenta de nuevo en unos segundos.")
            } else {
                call.application.log.error("Error no controlado: {}", e::class.qualifiedName)
                call.responderProblema(HttpStatusCode.InternalServerError, "Error interno")
            }
        }
        exception<Throwable> { call, e ->
            // Solo la clase: el mensaje puede traer datos de la petición.
            call.application.log.error("Error no controlado: {}", e::class.qualifiedName)
            call.responderProblema(HttpStatusCode.InternalServerError, "Error interno")
        }
    }
}
