package co.trayectago.backend

import co.trayectago.backend.usuarios.Usuario
import co.trayectago.backend.usuarios.aliasAleatorio
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
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
    fun `aliasAleatorio rellena con ceros`() {
        assertEquals("Usuario 0007", aliasAleatorio(7))
    }
}
