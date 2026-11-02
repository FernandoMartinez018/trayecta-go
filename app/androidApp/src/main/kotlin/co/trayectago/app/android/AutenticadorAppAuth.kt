package co.trayectago.app.android

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import co.trayectago.app.ConfiguracionApp
import co.trayectago.app.api.ErrorApi
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PREFERENCIAS = "trayecta_sesion"
private const val CLAVE_ESTADO = "auth_state"

/**
 * Sesión OIDC con AppAuth (Custom Tab + PKCE) contra el realm trayecta-go de Keycloak.
 * El estado vive en preferencias privadas, excluidas de los respaldos (ver data_extraction_rules).
 */
class AutenticadorAppAuth(actividad: ComponentActivity, private val config: ConfiguracionApp) : Autenticador {
    private val preferencias = actividad.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    // Creado en onCreate: así la Custom Tab lleva CustomTabsSession y Chrome confía en la app al volver.
    private val servicio = AuthorizationService(actividad)
    private val candado = Mutex()
    private var estado: AuthState = leerEstado()
    private var configuracion: AuthorizationServiceConfiguration? = null
    private var resultadoPendiente: CompletableDeferred<ActivityResult>? = null
    private val lanzador = actividad.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        resultadoPendiente?.complete(it)
    }

    init {
        actividad.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = servicio.dispose()
        })
    }

    override fun haySesion(): Boolean = estado.refreshToken != null

    override suspend fun iniciarSesion(opcion: OpcionIngreso): ResultadoIngreso {
        val conf = try {
            obtenerConfiguracion()
        } catch (e: AuthorizationException) {
            return ResultadoIngreso.Fallo(sinConexion = !e.esDeSesion())
        }
        val peticion = AuthorizationRequest.Builder(conf, config.clienteId, ResponseTypeValues.CODE, Uri.parse(config.redirect))
            .setScopes("openid", "offline_access")
            .apply {
                when (opcion) {
                    // login_hint es un parámetro propio de AppAuth: en additionalParameters lanza excepción.
                    is OpcionIngreso.Correo -> setLoginHint(opcion.correo)
                    OpcionIngreso.Google -> setAdditionalParameters(mapOf("kc_idp_hint" to "google"))
                    OpcionIngreso.General -> Unit
                }
            }
            .build()

        val espera = CompletableDeferred<ActivityResult>().also { resultadoPendiente = it }
        lanzador.launch(servicio.getAuthorizationRequestIntent(peticion))
        val datos = espera.await().data
        val respuesta = datos?.let { AuthorizationResponse.fromIntent(it) }
        if (respuesta == null) {
            val error = datos?.let { AuthorizationException.fromIntent(it) }
            return if (error == null || error == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW) {
                ResultadoIngreso.Cancelado
            } else {
                ResultadoIngreso.Fallo(sinConexion = !error.esDeSesion())
            }
        }

        return candado.withLock {
            estado = AuthState(respuesta, null)
            suspendCancellableCoroutine { cont ->
                servicio.performTokenRequest(respuesta.createTokenExchangeRequest()) { tokens, ex ->
                    estado.update(tokens, ex)
                    guardarEstado()
                    cont.resume(if (tokens != null) ResultadoIngreso.Exito else ResultadoIngreso.Fallo(sinConexion = ex?.esDeSesion() == false))
                }
            }
        }
    }

    override suspend fun tokenDeAcceso(forzarRenovacion: Boolean): String? = candado.withLock {
        if (estado.refreshToken == null) return@withLock null
        if (forzarRenovacion) estado.needsTokenRefresh = true
        suspendCancellableCoroutine { cont ->
            estado.performActionWithFreshTokens(servicio) { token, _, ex ->
                guardarEstado()
                when {
                    ex == null -> cont.resume(token)
                    // invalid_grant u otro rechazo de Keycloak: la sesión murió.
                    ex.esDeSesion() -> cont.resume(null)
                    // Red, timeout o Keycloak caído: la sesión se conserva.
                    else -> cont.resumeWithException(ErrorApi.SinConexion)
                }
            }
        }
    }

    override suspend fun cerrarSesion() {
        val refresco = candado.withLock {
            estado.refreshToken.also {
                estado = AuthState()
                preferencias.edit().remove(CLAVE_ESTADO).apply()
            }
        }
        // La sesión local se borra aunque la revocación falle (sin red, por ejemplo).
        if (refresco != null) runCatching { revocar(refresco) }
    }

    private suspend fun obtenerConfiguracion(): AuthorizationServiceConfiguration =
        configuracion ?: suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(Uri.parse("${config.urlAuth}/realms/trayecta-go")) { conf, ex ->
                if (conf != null) {
                    configuracion = conf
                    cont.resume(conf)
                } else {
                    cont.resumeWithException(ex ?: AuthorizationException.GeneralErrors.NETWORK_ERROR)
                }
            }
        }

    private suspend fun revocar(token: String) = withContext(Dispatchers.IO) {
        val conexion = URL("${config.urlAuth}/realms/trayecta-go/protocol/openid-connect/revoke").openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "POST"
            conexion.doOutput = true
            conexion.connectTimeout = 5_000
            conexion.readTimeout = 5_000
            conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            val cuerpo = "client_id=${codificar(config.clienteId)}&token=${codificar(token)}&token_type_hint=refresh_token"
            conexion.outputStream.use { it.write(cuerpo.toByteArray()) }
            conexion.responseCode
        } finally {
            conexion.disconnect()
        }
    }

    private fun leerEstado(): AuthState =
        preferencias.getString(CLAVE_ESTADO, null)?.let { runCatching { AuthState.jsonDeserialize(it) }.getOrNull() } ?: AuthState()

    private fun guardarEstado() {
        preferencias.edit().putString(CLAVE_ESTADO, estado.jsonSerializeString()).apply()
    }

    private fun codificar(valor: String) = URLEncoder.encode(valor, Charsets.UTF_8.name())

    /** Un rechazo OAuth de Keycloak (invalid_grant, access_denied…) significa sesión inválida; lo demás es transitorio. */
    private fun AuthorizationException.esDeSesion() =
        type == AuthorizationException.TYPE_OAUTH_TOKEN_ERROR || type == AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR
}
