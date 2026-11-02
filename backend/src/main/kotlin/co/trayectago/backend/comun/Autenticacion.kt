package co.trayectago.backend.comun

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import java.net.URI
import java.util.concurrent.TimeUnit

const val AUTENTICACION_KEYCLOAK = "keycloak"

/** JWKS de Keycloak con caché y límite de consultas; la URL puede ser interna aunque el issuer sea público. */
fun crearJwkProvider(c: ConfigOidc): JwkProvider = JwkProviderBuilder(URI(c.jwksUrl).toURL())
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(20, 1, TimeUnit.MINUTES)
    .timeouts(3_000, 3_000)
    .build()

fun Application.instalarAutenticacion(c: ConfigOidc, jwkProvider: JwkProvider) {
    install(Authentication) {
        jwt(AUTENTICACION_KEYCLOAK) {
            verifier(jwkProvider, c.issuer) {
                withAudience(c.audiencia)
                acceptLeeway(30)
            }
            validate { credencial -> credencial.payload.subject?.let { JWTPrincipal(credencial.payload) } }
            challenge { _, _ ->
                call.responderProblema(HttpStatusCode.Unauthorized, "No autenticado", "Falta el token o venció.")
            }
        }
    }
}

/** `sub` del token; solo se usa dentro de rutas autenticadas. */
val ApplicationCall.sujeto: String
    get() = principal<JWTPrincipal>()!!.subject!!
