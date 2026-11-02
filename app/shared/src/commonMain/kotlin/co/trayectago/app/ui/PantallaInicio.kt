package co.trayectago.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta

/** Inicio provisional: lo reemplaza la pantalla 4 (mapa) en H2. */
@Composable
fun PantallaInicio(alCerrarSesion: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(Espacios.space5),
        verticalArrangement = Arrangement.spacedBy(Espacios.space3, alignment = Alignment.CenterVertically),
    ) {
        Text(Textos.INICIO_TITULO, style = Trayecta.estilos.titleXl)
        Text(Textos.INICIO_CUERPO, style = Trayecta.estilos.bodyLg, color = Trayecta.colores.inkMuted)
        OutlinedButton(onClick = alCerrarSesion, Modifier.fillMaxWidth().heightIn(min = Tamanos.touchMin)) {
            Text(Textos.CERRAR_SESION, style = Trayecta.estilos.button)
        }
    }
}
