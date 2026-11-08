package co.trayectago.app

import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import co.trayectago.app.estado.ControladorApp
import co.trayectago.app.estado.Pantalla
import co.trayectago.app.ui.Textos
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ControladorAppTest {
    private fun TestScope.controlador(autenticador: AutenticadorFalso, manejador: MockRequestHandler = respuestaUsuario()) =
        ControladorApp(ServidorFalso(StandardTestDispatcher(testScheduler), manejador).cliente(autenticador), autenticador, this)

    @Test
    fun `sin sesión va a Registro`() = runTest {
        val c = controlador(AutenticadorFalso(sesion = false))

        c.arrancar()
        advanceUntilIdle()

        assertEquals(Pantalla.Registro(), c.pantalla.value)
    }

    @Test
    fun `con sesión y perfil va a Inicio`() = runTest {
        val c = controlador(AutenticadorFalso())

        c.arrancar()
        advanceUntilIdle()

        assertEquals("Usuario 0042", assertIs<Pantalla.Inicio>(c.pantalla.value).usuario.alias)
    }

    @Test
    fun `sesión vencida cierra sesión y va a Registro`() = runTest {
        val autenticador = AutenticadorFalso()
        val c = controlador(autenticador) { respond("", HttpStatusCode.Unauthorized) }

        c.arrancar()
        advanceUntilIdle()

        assertEquals(1, autenticador.cierres)
        assertEquals(Pantalla.Registro(), c.pantalla.value)
    }

    @Test
    fun `Keycloak o la red caídos conservan la sesión`() = runTest {
        val autenticador = AutenticadorFalso().apply { keycloakCaido = true }
        val c = controlador(autenticador)

        c.arrancar()
        advanceUntilIdle()
        assertEquals(Pantalla.SinConexion, c.pantalla.value)
        assertEquals(0, autenticador.cierres)

        autenticador.keycloakCaido = false
        c.reintentar()
        advanceUntilIdle()
        assertIs<Pantalla.Inicio>(c.pantalla.value)
    }

    @Test
    fun `un 503 del backend conserva la sesión`() = runTest {
        val autenticador = AutenticadorFalso()
        val c = controlador(autenticador) { respond("", HttpStatusCode.ServiceUnavailable) }

        c.arrancar()
        advanceUntilIdle()

        assertEquals(Pantalla.SinConexion, c.pantalla.value)
        assertEquals(0, autenticador.cierres)
    }

    @Test
    fun `ingreso cancelado vuelve a Registro sin error`() = runTest {
        val autenticador = AutenticadorFalso(sesion = false).apply { resultadoIngreso = ResultadoIngreso.Cancelado }
        val c = controlador(autenticador)

        c.ingresar(OpcionIngreso.General)
        advanceUntilIdle()

        assertEquals(Pantalla.Registro(), c.pantalla.value)
    }

    @Test
    fun `resultado de un ingreso anterior no reemplaza el ingreso más reciente`() = runTest {
        val primero = CompletableDeferred<ResultadoIngreso>()
        val segundo = CompletableDeferred<ResultadoIngreso>()
        var intentos = 0
        val autenticador = AutenticadorFalso(sesion = false)
        val api = ServidorFalso(StandardTestDispatcher(testScheduler), respuestaUsuario()).cliente(autenticador)
        val control = ControladorApp(api, object : co.trayectago.app.auth.Autenticador by autenticador {
            override suspend fun iniciarSesion(opcion: OpcionIngreso): ResultadoIngreso {
                autenticador.opciones += opcion
                return if (intentos++ == 0) primero.await() else segundo.await()
            }
        }, this)

        control.ingresar(OpcionIngreso.Google)
        runCurrent()
        control.ingresar(OpcionIngreso.General)
        runCurrent()
        segundo.complete(ResultadoIngreso.Cancelado)
        runCurrent()
        assertEquals(Pantalla.Registro(), control.pantalla.value)
        primero.complete(ResultadoIngreso.Exito)
        advanceUntilIdle()

        assertEquals(Pantalla.Registro(), control.pantalla.value)
    }

    @Test
    fun `correo inválido no inicia el ingreso`() = runTest {
        val autenticador = AutenticadorFalso(sesion = false)
        val c = controlador(autenticador)

        c.ingresar(OpcionIngreso.Correo("ana@"))
        advanceUntilIdle()

        assertEquals(emptyList(), autenticador.opciones)
    }

    @Test
    fun `correo válido se envía recortado`() = runTest {
        val autenticador = AutenticadorFalso(sesion = false)
        val c = controlador(autenticador)

        c.ingresar(OpcionIngreso.Correo(" ana@correo.com "))
        advanceUntilIdle()

        assertEquals(listOf<OpcionIngreso>(OpcionIngreso.Correo("ana@correo.com")), autenticador.opciones)
    }

    @Test
    fun `ingreso fallido muestra el error de ingreso`() = runTest {
        val autenticador = AutenticadorFalso(sesion = false).apply { resultadoIngreso = ResultadoIngreso.Fallo(sinConexion = true) }
        val c = controlador(autenticador)

        c.ingresar(OpcionIngreso.Google)
        advanceUntilIdle()

        assertEquals(Pantalla.Registro(error = Textos.ERROR_INGRESO), c.pantalla.value)
    }
}
