package co.trayectago.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Inicio provisional: lo reemplaza la pantalla 4 (mapa) en H2. */
@Composable
fun PantallaInicio(alCerrarSesion: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
    ) {
        Text(Textos.INICIO_TITULO, style = MaterialTheme.typography.headlineMedium)
        Text(Textos.INICIO_CUERPO, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = alCerrarSesion, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(Textos.CERRAR_SESION)
        }
    }
}
