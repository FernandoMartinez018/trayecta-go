package co.trayectago.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.trayectago.app.auth.OpcionIngreso

/** Registro mínimo para probar el ingreso; la HU-1.1 aplica el diseño de la pantalla 12. */
@Composable
fun PantallaRegistro(error: String?, alIngresar: (OpcionIngreso) -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically),
    ) {
        Text(Textos.REGISTRO_TITULO, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { alIngresar(OpcionIngreso.Google) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(Textos.CONTINUAR_GOOGLE)
        }
        TextButton(onClick = { alIngresar(OpcionIngreso.General) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(Textos.INICIA_SESION)
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
