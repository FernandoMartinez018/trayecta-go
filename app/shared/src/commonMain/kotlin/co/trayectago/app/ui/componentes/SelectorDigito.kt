package co.trayectago.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.Textos

/** Rejilla de 5 × 2 con los dígitos 0 a 9 (Main.dc.html): el elegido va en color de marca y los demás con borde. */
@Composable
fun SelectorDigito(elegido: Int?, alElegir: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Espacios.space2)) {
        (0..9 step 5).forEach { inicio ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Espacios.space2)) {
                (inicio until inicio + 5).forEach { digito ->
                    val seleccionado = elegido == digito
                    Button(
                        onClick = { alElegir(digito) },
                        modifier = Modifier.weight(1f).heightIn(min = Tamanos.touchMin).semantics {
                            contentDescription = Textos.placaTerminadaEn(digito)
                            selected = seleccionado
                        },
                        shape = RoundedCornerShape(Radios.radiusMd),
                        border = BorderStroke(BORDE_CONTROL, if (seleccionado) Trayecta.colores.brand else Trayecta.colores.lineStrong),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seleccionado) Trayecta.colores.brand else Trayecta.colores.surface,
                            contentColor = if (seleccionado) Trayecta.colores.onBrand else Trayecta.colores.ink,
                        ),
                    ) {
                        Text(digito.toString(), style = Trayecta.estilos.bodyLg.copy(fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal))
                    }
                }
            }
        }
    }
}
