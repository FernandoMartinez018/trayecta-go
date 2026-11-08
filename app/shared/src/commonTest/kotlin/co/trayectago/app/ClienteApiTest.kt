package co.trayectago.app

import co.trayectago.app.api.ErrorApi
import co.trayectago.app.api.Problema
import co.trayectago.app.api.Salud
import co.trayectago.app.api.Vehiculo
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClienteApiTest {
    @Test
    fun `salud decodifica la respuesta`() = runTest {
        val servidor = ServidorFalso { respond("""{"estado":"ok","version":"x"}""", HttpStatusCode.OK, JSON) }

        assertEquals(Salud("ok", "x"), servidor.cliente(AutenticadorFalso()).salud())
    }

    @Test
    fun `yo envía el token Bearer`() = runTest {
        val servidor = ServidorFalso(manejador = respuestaUsuario())

        servidor.cliente(AutenticadorFalso(token = "tk1")).yo()

        assertEquals("Bearer tk1", servidor.peticiones.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `vehiculo con 404 es null`() = runTest {
        val servidor = ServidorFalso { respond("""{"title":"No encontrado","status":404}""", HttpStatusCode.NotFound, PROBLEM_JSON) }

        assertEquals(null, servidor.cliente(AutenticadorFalso()).vehiculo())
    }

    @Test
    fun `guardarVehiculo envía el JSON del contrato`() = runTest {
        val servidor = ServidorFalso { respond("""{"tipo":"moto","ultimoDigitoPlaca":4,"llevaParrillero":true}""", HttpStatusCode.OK, JSON) }

        assertEquals(Vehiculo("moto", 4, true), servidor.cliente(AutenticadorFalso()).guardarVehiculo(Vehiculo("moto", 4, true)))
        assertEquals("{\"tipo\":\"moto\",\"ultimoDigitoPlaca\":4,\"llevaParrillero\":true}", ((servidor.peticiones.single().body as OutgoingContent.ByteArrayContent).bytes()).decodeToString())
    }

    @Test
    fun `un 401 renueva el token una vez y reintenta`() = runTest {
        var llamadas = 0
        val servidor = ServidorFalso {
            llamadas++
            if (llamadas == 1) respond("", HttpStatusCode.Unauthorized) else respond(USUARIO_JSON, HttpStatusCode.OK, JSON)
        }
        val autenticador = AutenticadorFalso()

        val usuario = servidor.cliente(autenticador).yo()

        assertEquals("Usuario 0042", usuario.alias)
        assertEquals(2, servidor.peticiones.size)
        assertEquals(1, autenticador.renovacionesForzadas)
        assertEquals("Bearer tk2", servidor.peticiones.last().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `401 después de renovar es NoAutenticado`() = runTest {
        val servidor = ServidorFalso { respond("", HttpStatusCode.Unauthorized) }

        assertFailsWith<ErrorApi.NoAutenticado> { servidor.cliente(AutenticadorFalso()).yo() }
        assertEquals(2, servidor.peticiones.size)
    }

    @Test
    fun `sin token es NoAutenticado sin llamar al servidor`() = runTest {
        val servidor = ServidorFalso(manejador = respuestaUsuario())

        assertFailsWith<ErrorApi.NoAutenticado> { servidor.cliente(AutenticadorFalso(token = null)).yo() }
        assertEquals(0, servidor.peticiones.size)
    }

    @Test
    fun `falla de red es SinConexion`() = runTest {
        val servidor = ServidorFalso { throw IOException("sin red") }

        assertFailsWith<ErrorApi.SinConexion> { servidor.cliente(AutenticadorFalso()).yo() }
    }

    @Test
    fun `503 conserva el estado y el problema`() = runTest {
        val servidor = ServidorFalso {
            respond("""{"type":"about:blank","title":"Servicio no disponible","status":503}""", HttpStatusCode.ServiceUnavailable, PROBLEM_JSON)
        }

        val error = assertFailsWith<ErrorApi.Servidor> { servidor.cliente(AutenticadorFalso()).yo() }

        assertEquals(ErrorApi.Servidor(503, Problema("about:blank", "Servicio no disponible", 503)), error)
    }
}
