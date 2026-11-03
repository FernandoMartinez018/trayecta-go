package co.trayectago.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta

/** Registro mínimo para probar el ingreso; la HU-1.1 aplica el diseño de la pantalla 12. */
@Composable
fun PantallaRegistro(error: String?, alIngresar: (OpcionIngreso) -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Espacios.space5),
        verticalArrangement = Arrangement.spacedBy(Espacios.space3, alignment = Alignment.CenterVertically),
    ) {
        Text(Textos.REGISTRO_TITULO, style = Trayecta.estilos.titleXl)
        Button(onClick = { alIngresar(OpcionIngreso.Google) }, Modifier.fillMaxWidth().heightIn(min = Tamanos.touchMin)) {
            Text(Textos.CONTINUAR_GOOGLE, style = Trayecta.estilos.button)
        }
        TextButton(onClick = { alIngresar(OpcionIngreso.General) }, Modifier.fillMaxWidth().heightIn(min = Tamanos.touchMin)) {
            Text(Textos.INICIA_SESION, style = Trayecta.estilos.button)
        }
        if (error != null) {
            Text(error, style = Trayecta.estilos.body, color = Trayecta.colores.critical)
        }
    }
}
