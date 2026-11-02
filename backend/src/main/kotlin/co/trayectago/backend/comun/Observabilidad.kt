package co.trayectago.backend.comun

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import java.util.UUID

private val ID_VALIDO = Regex("^[A-Za-z0-9-]{1,64}$")

fun Application.instalarObservabilidad() {
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        verify { ID_VALIDO.matches(it) }
        // Siempre hay id: un proveedor de MDC nulo hace que Ktor 3.6.0 repita el de otra petición (KTOR-9980).
        generate { UUID.randomUUID().toString() }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        callIdMdc("idPeticion")
        // Ruta sin query string: la query puede traer datos personales (búsquedas, coordenadas).
        format { call ->
            "${call.request.httpMethod.value} ${call.request.path()} ${call.response.status()?.value} ${call.processingTimeMillis()}ms"
        }
    }
}
