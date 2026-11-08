package co.trayectago.app.ui.componentes

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Medidas del prototipo que no son token: raya de 6 dp con hueco de 4 dp.
private val RAYA = 6.dp
private val HUECO = 4.dp

/** Borde discontinuo de BORDE_CONTROL (Compose no lo trae): se traza hacia adentro para que no se recorte. */
fun Modifier.bordeDiscontinuo(color: Color, radio: Dp): Modifier = drawBehind {
    val trazo = BORDE_CONTROL.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(trazo / 2, trazo / 2),
        size = Size(size.width - trazo, size.height - trazo),
        cornerRadius = CornerRadius(radio.toPx()),
        style = Stroke(trazo, pathEffect = PathEffect.dashPathEffect(floatArrayOf(RAYA.toPx(), HUECO.toPx()))),
    )
}
