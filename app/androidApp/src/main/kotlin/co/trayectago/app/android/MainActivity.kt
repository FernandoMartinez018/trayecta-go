package co.trayectago.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.trayectago.app.BuildConfig
import co.trayectago.app.ConfiguracionApp
import co.trayectago.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val config = ConfiguracionApp(
            urlApi = BuildConfig.URL_API,
            urlAuth = BuildConfig.URL_AUTH,
            redirect = "$packageName:/oauth2redirect",
        )
        // Se crea en onCreate: registra su ActivityResultLauncher antes de que la actividad arranque.
        val autenticador = AutenticadorAppAuth(this, config)
        setContent { App(config, autenticador) }
    }
}
