package co.trayectago.backend

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.get
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ProblemasTest {
    @Test
    fun `ruta inexistente responde 404 problem+json`() = pruebaDeApp { cliente ->
        val r = cliente.get("/v1/nada")

        assertEquals(HttpStatusCode.NotFound, r.status)
        assertEquals("application/problem+json", r.contentType()?.withoutParameters()?.toString())
        val cuerpo = Json.parseToJsonElement(r.bodyAsText()).jsonObject
        assertEquals("No encontrado", cuerpo["title"]!!.jsonPrimitive.content)
        assertEquals(404, cuerpo["status"]!!.jsonPrimitive.int)
    }

    @Test
    fun `error inesperado responde 500 sin detalles internos`() = pruebaDeApp(
        rutasExtra = { get("/v1/explota") { error("secreto interno") } },
    ) { cliente ->
        val r = cliente.get("/v1/explota")

        assertEquals(HttpStatusCode.InternalServerError, r.status)
        assertEquals("application/problem+json", r.contentType()?.withoutParameters()?.toString())
        val texto = r.bodyAsText()
        assertEquals("Error interno", Json.parseToJsonElement(texto).jsonObject["title"]!!.jsonPrimitive.content)
        assertFalse(texto.contains("secreto interno"))
    }
}
