package co.trayectago.app

import co.trayectago.app.api.ClienteApi
import co.trayectago.app.api.ErrorApi
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import co.trayectago.app.permisos.GestorPermisos
import co.trayectago.app.permisos.Permiso
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineDispatcher

/** Permisos del sistema controlables: cuáles ya están concedidos y si el usuario acepta o niega cada solicitud. */
class GestorPermisosFalso(concedidos: Set<Permiso> = emptySet(), private val acepta: Boolean = true) : GestorPermisos {
    private val concedidos = concedidos.toMutableSet()
    val solicitudes = mutableListOf<Permiso>()

    override fun concedido(p: Permiso) = p in concedidos

    override suspend fun solicitar(p: Permiso): Boolean {
        solicitudes += p
        if (acepta) concedidos += p
        return acepta
    }
}

/** Autenticador controlable: sesión, tokens y resultado del ingreso a gusto de cada prueba. */
class AutenticadorFalso(
    var sesion: Boolean = true,
    var token: String? = "tk1",
    var tokenTrasRenovar: String? = "tk2",
) : Autenticador {
    var resultadoIngreso: ResultadoIngreso = ResultadoIngreso.Exito
    var keycloakCaido = false
    var renovacionesForzadas = 0
    var cierres = 0
    val opciones = mutableListOf<OpcionIngreso>()

    override fun haySesion() = sesion

    override suspend fun iniciarSesion(opcion: OpcionIngreso): ResultadoIngreso {
        opciones += opcion
        if (resultadoIngreso == ResultadoIngreso.Exito) sesion = true
        return resultadoIngreso
    }

    override suspend fun tokenDeAcceso(forzarRenovacion: Boolean): String? {
        if (keycloakCaido) throw ErrorApi.SinConexion
        if (forzarRenovacion) {
            renovacionesForzadas++
            token = tokenTrasRenovar
        }
        return token
    }

    override suspend fun cerrarSesion() {
        cierres++
        sesion = false
        token = null
    }
}

const val USUARIO_JSON =
    """{"id":"7d6f0e1a-1b2c-4d5e-8f90-123456789abc","alias":"Usuario 0042","nivel":"1","puntos":0,"reportesConfirmados":0,""" +
        """"preferencias":{"manosLibres":true,"modoSeguro":false,"avisosLluvia":true,"anticipacionAvisoMin":5},"creadoEn":"2026-11-01T12:00:00Z"}"""

val JSON = headersOf(HttpHeaders.ContentType, "application/json")
val PROBLEM_JSON = headersOf(HttpHeaders.ContentType, "application/problem+json")

/**
 * Motor falso que registra las peticiones recibidas. Con `despachador` de prueba, las peticiones
 * corren en el reloj virtual de runTest y advanceUntilIdle las espera.
 */
class ServidorFalso(despachador: CoroutineDispatcher? = null, private val manejador: MockRequestHandler) {
    val peticiones = mutableListOf<HttpRequestData>()
    val motor = MockEngine(
        MockEngineConfig().apply {
            despachador?.let { dispatcher = it }
            addHandler { peticion ->
                peticiones += peticion
                manejador(peticion)
            }
        },
    )

    fun cliente(autenticador: Autenticador) = ClienteApi("http://api.prueba", autenticador, motor)
}

fun respuestaUsuario(): MockRequestHandler = { respond(USUARIO_JSON, HttpStatusCode.OK, JSON) }
