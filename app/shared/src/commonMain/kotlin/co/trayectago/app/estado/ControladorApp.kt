package co.trayectago.app.estado

import co.trayectago.app.api.ClienteApi
import co.trayectago.app.api.ErrorApi
import co.trayectago.app.api.Usuario
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import co.trayectago.app.ui.Textos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        alcance.launch { cargarSesion() }
    }

    fun reintentar() = arrancar()

    fun ingresar(opcion: OpcionIngreso) {
        alcance.launch {
            when (autenticador.iniciarSesion(opcion)) {
                ResultadoIngreso.Exito -> cargarSesion()
                ResultadoIngreso.Cancelado -> estado.value = Pantalla.Registro()
                is ResultadoIngreso.Fallo -> estado.value = Pantalla.Registro(error = Textos.ERROR_INGRESO)
            }
        }
    }

    fun cerrarSesion() {
        alcance.launch {
            autenticador.cerrarSesion()
            estado.value = Pantalla.Registro()
        }
    }

    private suspend fun cargarSesion() {
        if (!autenticador.haySesion()) {
            estado.value = Pantalla.Registro()
            return
        }
        estado.value = try {
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
