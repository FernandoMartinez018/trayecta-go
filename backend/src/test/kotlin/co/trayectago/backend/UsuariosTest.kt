package co.trayectago.backend

import co.trayectago.backend.usuarios.Usuario
import co.trayectago.backend.usuarios.aliasAleatorio
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonObject
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UsuariosTest {
    @BeforeTest
    fun limpiar() = limpiarBase(baseDePruebas())

    private fun vehiculoEsperado(digito: Int, parrillero: Boolean): String =
        """{"tipo":"moto","ultimoDigitoPlaca":$digito,"llevaParrillero":$parrillero}"""

    private fun serializar(json: String): String =
        Json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), Json.parseToJsonElement(json).jsonObject)

    private fun cuentaUsuarios(keycloakId: String): Int = baseDePruebas().connection.use { c ->
        c.prepareStatement("SELECT count(*) FROM usuario WHERE keycloak_id = ?").use { s ->
            s.setString(1, keycloakId)
            s.executeQuery().use { r -> r.next(); r.getInt(1) }
        }
    }

    @Test
    fun `el primer acceso crea el usuario`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar()) }

        assertEquals(HttpStatusCode.OK, r.status)
        val u = r.body<Usuario>()
        assertTrue(Regex("^Usuario \\d{4}$").matches(u.alias), "alias inesperado: ${u.alias}")
        assertEquals("1", u.nivel)
        assertEquals(0, u.puntos)
        assertEquals(0, u.reportesConfirmados)
        assertEquals(true, u.preferencias.manosLibres)
        assertEquals(5, u.preferencias.anticipacionAvisoMin)
        UUID.fromString(u.id)
        Instant.parse(u.creadoEn)
    }

    @Test
    fun `accesos repetidos devuelven el mismo usuario`() = pruebaDeApp { cliente ->
        val primero = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar()) }.body<Usuario>()
        val segundo = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar()) }.body<Usuario>()

        assertEquals(primero.id, segundo.id)
        assertEquals(primero.alias, segundo.alias)
        assertEquals(1, cuentaUsuarios("usuario-1"))
    }

    @Test
    fun `accesos concurrentes del mismo usuario nuevo no fallan`() = pruebaDeApp { cliente ->
        val token = TokensDePrueba.firmar(sub = "usuario-concurrente")

        val respuestas = coroutineScope {
            List(10) { async { cliente.get("/v1/me") { bearerAuth(token) } } }.awaitAll()
        }

        assertTrue(respuestas.all { it.status == HttpStatusCode.OK }, respuestas.map { it.status }.toString())
        assertEquals(1, respuestas.map { it.body<Usuario>().id }.toSet().size)
        assertEquals(1, cuentaUsuarios("usuario-concurrente"))
    }

    @Test
    fun `usuarios distintos obtienen cuentas distintas`() = pruebaDeApp { cliente ->
        val a = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(sub = "a")) }.body<Usuario>()
        val b = cliente.get("/v1/me") { bearerAuth(TokensDePrueba.firmar(sub = "b")) }.body<Usuario>()

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `sin vehículo responde 404 problem+json`() = pruebaDeApp { cliente ->
        val respuesta = cliente.get("/v1/me/vehiculo") { bearerAuth(TokensDePrueba.firmar()) }

        assertEquals(HttpStatusCode.NotFound, respuesta.status)
        assertTrue(respuesta.headers["Content-Type"].orEmpty().contains("application/problem+json"))
    }

    @Test
    fun `PUT con dígito fuera de rango responde 400`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar())
            contentType(ContentType.Application.Json)
            setBody("""{"tipo":"moto","ultimoDigitoPlaca":10}""")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
        assertTrue(respuesta.body<String>().contains("ultimoDigitoPlaca debe ser un número entre 0 y 9"))
    }

    @Test
    fun `PUT con JSON roto responde 400`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar())
            contentType(ContentType.Application.Json)
            setBody("no es json")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
    }

    @Test
    fun `PUT sin cuerpo responde 400`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar())
            contentType(ContentType.Application.Json)
            setBody("")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
    }

    @Test
    fun `PUT con un Content-Type que no es JSON responde 400 y no 500`() = pruebaDeApp { cliente ->
        listOf(ContentType.Text.Plain, ContentType.Application.FormUrlEncoded, ContentType.Application.OctetStream).forEach { tipo ->
            val respuesta = cliente.put("/v1/me/vehiculo") {
                bearerAuth(TokensDePrueba.firmar())
                contentType(tipo)
                setBody("""{"tipo":"moto","ultimoDigitoPlaca":4}""")
            }

            assertEquals(HttpStatusCode.BadRequest, respuesta.status, "Content-Type: $tipo")
            assertTrue(respuesta.headers["Content-Type"].orEmpty().contains("application/problem+json"), "Content-Type: $tipo")
        }
    }

    @Test
    fun `PUT sin token responde 401`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            contentType(ContentType.Application.Json)
            setBody("""{"tipo":"moto","ultimoDigitoPlaca":4}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, respuesta.status)
    }

    @Test
    fun `PUT con tipo carro responde 400`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar())
            contentType(ContentType.Application.Json)
            setBody("""{"tipo":"carro","ultimoDigitoPlaca":4}""")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
        assertTrue(respuesta.body<String>().contains("tipo debe ser"))
    }

    @Test
    fun `PUT con el dígito como texto responde 400`() = pruebaDeApp { cliente ->
        val respuesta = cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar())
            contentType(ContentType.Application.Json)
            setBody("""{"tipo":"moto","ultimoDigitoPlaca":"4"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, respuesta.status)
    }

    @Test
    fun `PUT guarda y GET devuelve el vehículo activo`() = pruebaDeApp { cliente ->
        val token = TokensDePrueba.firmar()
        val entrada = """{"tipo":"moto","ultimoDigitoPlaca":4,"llevaParrillero":true}"""
        val guardado = cliente.put("/v1/me/vehiculo") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(entrada)
        }

        assertEquals(HttpStatusCode.OK, guardado.status)
        assertEquals(vehiculoEsperado(4, true), serializar(guardado.bodyAsText()))
        val consultado = cliente.get("/v1/me/vehiculo") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, consultado.status)
        assertEquals(vehiculoEsperado(4, true), serializar(consultado.bodyAsText()))
    }

    @Test
    fun `un segundo PUT reemplaza el activo`() = pruebaDeApp { cliente ->
        val token = TokensDePrueba.firmar()
        fun cuerpo(digito: Int) = """{"tipo":"moto","ultimoDigitoPlaca":$digito,"llevaParrillero":false}"""
        for (digito in listOf(4, 7)) {
            cliente.put("/v1/me/vehiculo") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(cuerpo(digito))
            }
        }
        assertEquals(cuerpo(7), cliente.get("/v1/me/vehiculo") { bearerAuth(token) }.body<String>())
        val activos = baseDePruebas().connection.use { c ->
            c.prepareStatement(
                "SELECT count(*) FROM vehiculo v JOIN usuario u ON u.id = v.usuario_id WHERE u.keycloak_id = ? AND v.activo",
            ).use { s ->
                s.setString(1, "usuario-1")
                s.executeQuery().use { r -> r.next(); r.getInt(1) }
            }
        }
        assertEquals(1, activos)
    }

    @Test
    fun `vehículo de un usuario no lo ve otro`() = pruebaDeApp { cliente ->
        cliente.put("/v1/me/vehiculo") {
            bearerAuth(TokensDePrueba.firmar(sub = "usuario-a"))
            contentType(ContentType.Application.Json)
            setBody("""{"tipo":"moto","ultimoDigitoPlaca":4}""")
        }
        val respuesta = cliente.get("/v1/me/vehiculo") { bearerAuth(TokensDePrueba.firmar(sub = "usuario-b")) }

        assertEquals(HttpStatusCode.NotFound, respuesta.status)
    }

    @Test
    fun `aliasAleatorio rellena con ceros`() {
        assertEquals("Usuario 0007", aliasAleatorio(7))
    }
}
