package co.trayectago.app.ui.componentes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import co.trayectago.app.shared.resources.Res
import co.trayectago.app.shared.resources.simbolo_cronometro_t
import org.jetbrains.compose.resources.painterResource

/** Símbolo Cronómetro T; decorativo, porque siempre va junto al nombre de la app. */
@Composable
fun SimboloCronometroT(color: Color, tamano: Dp) {
    Icon(painterResource(Res.drawable.simbolo_cronometro_t), contentDescription = null, tint = color, modifier = Modifier.size(tamano))
}
