package co.trayectago.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.Textos

// Medida del prototipo (Main.dc.html) que no es token: barras de 6 dp con 6 dp entre ellas.
private val ALTO_BARRA = 6.dp
private const val PASOS = 3

/** «Paso N de 3» con una barra de 3 segmentos: los primeros N en color de marca. */
@Composable
fun IndicadorPasos(paso: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(Espacios.space2)) {
        Text(Textos.paso(paso, PASOS), style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ALTO_BARRA)) {
            repeat(PASOS) { indice ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(ALTO_BARRA)
                        .clip(RoundedCornerShape(Radios.radiusFull))
                        .background(if (indice < paso) Trayecta.colores.brand else Trayecta.colores.line),
                )
            }
        }
    }
}
