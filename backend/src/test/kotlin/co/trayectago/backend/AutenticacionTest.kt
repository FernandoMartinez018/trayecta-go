package co.trayectago.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.NetworkException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class AutenticacionTest {
    @BeforeTest
    fun limpiar() = limpiarBase(baseDePruebas())

    private suspend fun assertProblema(r: HttpResponse, estado: HttpStatusCode, titulo: String) {
        assertEquals(estado, r.status, "cuerpo: ${r.bodyAsText()}")
        assertEquals("application/problem+json", r.contentType()?.withoutParameters()?.toString())
        assertEquals(titulo, Json.parseToJsonElement(r.bodyAsText()).jsonObject["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun `sin token responde 401 problem+json`() = pruebaDeApp { cliente ->
        assertProblema(cliente.get("/v1/me"), HttpStatusCode.Unauthorized, "No autenticado")
    }

    @Test
    fun `token con otra audiencia responde 401`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(aud = "otra-api")) }
        assertProblema(r, HttpStatusCode.Unauthorized, "No autenticado")
    }

    @Test
    fun `token de otro emisor responde 401`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(iss = "http://otro/realms/x")) }
        assertProblema(r, HttpStatusCode.Unauthorized, "No autenticado")
    }

    @Test
    fun `token vencido responde 401`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(expiraEn = (-2).minutes)) }
        assertProblema(r, HttpStatusCode.Unauthorized, "No autenticado")
    }

    @Test
    fun `token firmado con otra llave responde 401`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(llave = TokensDePrueba.nuevaLlave())) }
        assertProblema(r, HttpStatusCode.Unauthorized, "No autenticado")
    }

    @Test
    fun `si el JWKS no responde se devuelve 503 y no 401`() = pruebaDeApp(
        jwkProvider = JwkProvider { throw NetworkException("caído", null) },
    ) { cliente ->
        // Un 401 haría que la app cierre la sesión del usuario por una caída de Keycloak.
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar()) }
        assertProblema(r, HttpStatusCode.ServiceUnavailable, "Servicio no disponible")
    }
}
