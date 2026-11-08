package co.trayectago.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta

/** Grosor del borde de controles (diseno.md: borde line-strong de 1,5 dp); no es un token. */
val BORDE_CONTROL = 1.5.dp

@Composable
fun BotonPrincipal(texto: String, alPulsar: () -> Unit, habilitado: Boolean = true, modifier: Modifier = Modifier) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = Tamanos.touchMin),
        shape = RoundedCornerShape(Radios.radiusMd),
        colors = ButtonDefaults.buttonColors(containerColor = Trayecta.colores.brand, contentColor = Trayecta.colores.onBrand),
    ) {
        Text(texto, style = Trayecta.estilos.button)
    }
}

@Composable
fun BotonSecundario(texto: String, alPulsar: () -> Unit, habilitado: Boolean = true, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = Tamanos.touchMin),
        shape = RoundedCornerShape(Radios.radiusMd),
        border = BorderStroke(BORDE_CONTROL, Trayecta.colores.lineStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Trayecta.colores.ink),
    ) {
        Text(texto, style = Trayecta.estilos.button)
    }
}

@Composable
fun BotonTexto(texto: String, alPulsar: () -> Unit, habilitado: Boolean = true, modifier: Modifier = Modifier) {
    TextButton(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.heightIn(min = Tamanos.touchMin),
        shape = RoundedCornerShape(Radios.radiusMd),
        colors = ButtonDefaults.textButtonColors(contentColor = Trayecta.colores.brandTintInk),
    ) {
        Text(texto, style = Trayecta.estilos.button)
    }
}
