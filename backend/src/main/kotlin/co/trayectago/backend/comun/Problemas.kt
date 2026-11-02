package co.trayectago.backend.comun

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
        exception<Throwable> { call, e ->
            // Solo la clase: el mensaje puede traer datos de la petición.
            call.application.log.error("Error no controlado: {}", e::class.qualifiedName)
            call.responderProblema(HttpStatusCode.InternalServerError, "Error interno")
        }
    }
}
