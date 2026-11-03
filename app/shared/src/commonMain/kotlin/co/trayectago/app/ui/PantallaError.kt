package co.trayectago.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Estado de error con reintento opcional (patrón de la pantalla 20). */
@Composable
fun PantallaError(titulo: String, cuerpo: String?, alReintentar: (() -> Unit)?) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        if (cuerpo != null) Text(cuerpo, style = MaterialTheme.typography.bodyLarge)
        if (alReintentar != null) {
            Button(onClick = alReintentar, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(Textos.REINTENTAR)
            }
        }
    }
}

/** Mientras se revisa la sesión; la HU-1.1 lo reemplaza por la pantalla de inicio de marca. */
@Composable
fun PantallaCargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
