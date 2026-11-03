package co.trayectago.app.estado

import co.trayectago.app.api.ClienteApi
import co.trayectago.app.api.ErrorApi
import co.trayectago.app.api.Usuario
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import co.trayectago.app.ingreso.correoValido
import co.trayectago.app.ui.Textos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// La pantalla de inicio se ve al menos este tiempo, para que no parpadee.
private const val SPLASH_MINIMO_MS = 600L

sealed interface Pantalla {
    data object Splash : Pantalla
    data class Registro(val error: String? = null) : Pantalla
    data class Inicio(val usuario: Usuario) : Pantalla
    data object SinConexion : Pantalla
}

/** Decide qué pantalla mostrar. Navegación por estado: no hay navegación multiplataforma estable. */
class ControladorApp(
    private val api: ClienteApi,
    private val autenticador: Autenticador,
    private val alcance: CoroutineScope,
) {
    private val estado = MutableStateFlow<Pantalla>(Pantalla.Splash)
    val pantalla: StateFlow<Pantalla> = estado.asStateFlow()

    fun arrancar() {
        val desdeSplash = estado.value == Pantalla.Splash
        alcance.launch {
            val pausa = if (desdeSplash) launch { delay(SPLASH_MINIMO_MS) } else null
            val destino = destinoDeSesion()
            pausa?.join()
            estado.value = destino
        }
    }

    fun reintentar() = arrancar()

    fun ingresar(opcion: OpcionIngreso) {
        val elegida = when (opcion) {
            is OpcionIngreso.Correo -> if (correoValido(opcion.correo)) OpcionIngreso.Correo(opcion.correo.trim()) else return
            else -> opcion
        }
        alcance.launch {
            estado.value = when (autenticador.iniciarSesion(elegida)) {
                ResultadoIngreso.Exito -> destinoDeSesion()
                ResultadoIngreso.Cancelado -> Pantalla.Registro()
                is ResultadoIngreso.Fallo -> Pantalla.Registro(error = Textos.ERROR_INGRESO)
            }
        }
    }

    fun cerrarSesion() {
        alcance.launch {
            autenticador.cerrarSesion()
            estado.value = Pantalla.Registro()
        }
    }

    private suspend fun destinoDeSesion(): Pantalla {
        if (!autenticador.haySesion()) return Pantalla.Registro()
        return try {
            Pantalla.Inicio(api.yo())
        } catch (e: ErrorApi.NoAutenticado) {
            autenticador.cerrarSesion()
            Pantalla.Registro()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Red, Keycloak o el backend caídos: la sesión se conserva y se ofrece reintentar.
            Pantalla.SinConexion
        }
    }
}
