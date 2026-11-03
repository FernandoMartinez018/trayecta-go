package co.trayectago.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import co.trayectago.app.ConfiguracionApp
import co.trayectago.app.api.ClienteApi
import co.trayectago.app.api.motorHttp
import co.trayectago.app.auth.Autenticador
import co.trayectago.app.estado.ControladorApp
import co.trayectago.app.estado.Pantalla
import co.trayectago.app.tema.TemaTrayecta
import co.trayectago.app.tema.Trayecta

@Composable
fun App(config: ConfiguracionApp, autenticador: Autenticador) {
    TemaTrayecta {
        Surface(Modifier.fillMaxSize(), color = Trayecta.colores.surface, contentColor = Trayecta.colores.ink) {
            if (config.urlApi.isBlank() || config.urlAuth.isBlank()) {
                PantallaError(Textos.FALTA_CONFIGURACION, cuerpo = null, alReintentar = null)
            } else {
                Navegacion(config, autenticador)
            }
        }
    }
}

@Composable
private fun Navegacion(config: ConfiguracionApp, autenticador: Autenticador) {
    val alcance = rememberCoroutineScope()
    val controlador = remember { ControladorApp(ClienteApi(config.urlApi, autenticador, motorHttp()), autenticador, alcance) }
    LaunchedEffect(controlador) { controlador.arrancar() }
    val pantalla by controlador.pantalla.collectAsState()

    when (val actual = pantalla) {
        Pantalla.Splash -> PantallaCargando()
        is Pantalla.Registro -> PantallaRegistro(actual.error, alIngresar = controlador::ingresar)
        is Pantalla.Inicio -> PantallaInicio(alCerrarSesion = controlador::cerrarSesion)
        Pantalla.SinConexion -> PantallaError(Textos.SIN_CONEXION_TITULO, Textos.SIN_CONEXION_CUERPO, controlador::reintentar)
    }
}
