package co.trayectago.app.estado

import co.trayectago.app.api.ClienteApi
import co.trayectago.app.api.ErrorApi
import co.trayectago.app.api.Usuario
import co.trayectago.app.api.Vehiculo
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.auth.ResultadoIngreso
import co.trayectago.app.ingreso.correoValido
import co.trayectago.app.permisos.GestorPermisos
import co.trayectago.app.permisos.Permiso
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
    data class Vehiculo(val guardando: Boolean = false, val error: String? = null) : Pantalla
    data class Permisos(val concedidos: Set<Permiso> = emptySet()) : Pantalla
    data object SinConexion : Pantalla
}

/** Decide qué pantalla mostrar. Navegación por estado: no hay navegación multiplataforma estable. */
class ControladorApp(
    private val api: ClienteApi,
    private val autenticador: Autenticador,
    private val permisos: GestorPermisos,
    private val alcance: CoroutineScope,
) {
    private val estado = MutableStateFlow<Pantalla>(Pantalla.Splash)
    private var intentoIngreso = 0
    val pantalla: StateFlow<Pantalla> = estado.asStateFlow()

    fun arrancar() {
        val desdeSplash = estado.value == Pantalla.Splash
        alcance.launch {
            val pausa = if (desdeSplash) launch { delay(SPLASH_MINIMO_MS) } else null
            val destino = destinoDeSesionConVehiculo()
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
        val intento = ++intentoIngreso
        alcance.launch {
            val resultado = autenticador.iniciarSesion(elegida)
            val destino = when (resultado) {
                ResultadoIngreso.Exito -> destinoDeSesionConVehiculo()
                ResultadoIngreso.Cancelado -> Pantalla.Registro()
                is ResultadoIngreso.Fallo -> Pantalla.Registro(error = Textos.ERROR_INGRESO)
            }
            if (intento == intentoIngreso) estado.value = destino
        }
    }

    fun guardarVehiculo(digito: Int, parrillero: Boolean) {
        alcance.launch {
            estado.value = Pantalla.Vehiculo(guardando = true)
            try {
                api.guardarVehiculo(Vehiculo(ultimoDigitoPlaca = digito, llevaParrillero = parrillero))
                // Un guardado que termina tarde no revierte un cierre de sesión.
                estado.value = if (autenticador.haySesion()) Pantalla.Permisos(permisosConcedidos()) else Pantalla.Registro()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                estado.value = Pantalla.Vehiculo(error = Textos.ERROR_GUARDAR_VEHICULO)
            }
        }
    }

    fun solicitarPermiso(p: Permiso) {
        alcance.launch {
            val concedido = try {
                permisos.solicitar(p)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            val actual = estado.value
            if (actual is Pantalla.Permisos) {
                estado.value = Pantalla.Permisos(if (concedido) actual.concedidos + p else actual.concedidos - p)
            }
        }
    }

    /** «Continuar» y «Ahora no»: con o sin permisos concedidos se sigue a Inicio. */
    fun terminarPermisos() {
        alcance.launch { estado.value = destinoDeSesionConVehiculo() }
    }

    private fun permisosConcedidos(): Set<Permiso> = Permiso.entries.filter(permisos::concedido).toSet()

    fun cerrarSesion() {
        alcance.launch {
            autenticador.cerrarSesion()
            estado.value = Pantalla.Registro()
        }
    }

    private suspend fun destinoDeSesionConVehiculo(): Pantalla {
        if (!autenticador.haySesion()) return Pantalla.Registro()
        return try {
            val usuario = api.yo()
            if (api.vehiculo() == null) Pantalla.Vehiculo() else Pantalla.Inicio(usuario)
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
