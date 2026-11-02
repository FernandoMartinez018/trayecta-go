package co.trayectago.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaludTest {
    @Test
    fun `responde ok con la versión cuando la base responde`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/salud")

        assertEquals(HttpStatusCode.OK, r.status)
        assertTrue(r.contentType()!!.match(ContentType.Application.Json))
        assertEquals("""{"estado":"ok","version":"pruebas"}""", r.bodyAsText())
    }

    @Test
    fun `responde degradado si la base no responde`() {
        // Una base inalcanzable de verdad: puerto 1 cerrado y sin reintentos largos.
        val caida = HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://127.0.0.1:1/nada_pruebas"
            initializationFailTimeout = -1
            connectionTimeout = 250
        })
        caida.use { ds ->
            pruebaDeApp(ds) { cliente ->
                val r = cliente.get("/v1/salud")

                assertEquals(HttpStatusCode.OK, r.status)
                assertEquals("""{"estado":"degradado","version":"pruebas"}""", r.bodyAsText())
            }
        }
    }

    @Test
    fun `devuelve el X-Request-Id recibido`() = pruebaDeApp { cliente ->
        val conId = cliente.get("/v1/salud") { header(HttpHeaders.XRequestId, "abc-123") }
        val sinId = cliente.get("/v1/salud")

        assertEquals("abc-123", conId.headers[HttpHeaders.XRequestId])
        assertFalse(sinId.headers[HttpHeaders.XRequestId].isNullOrEmpty())
    }
}
