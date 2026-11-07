package co.trayectago.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import co.trayectago.app.estado.Pantalla
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.componentes.BORDE_CONTROL
import co.trayectago.app.ui.componentes.BotonPrincipal
import co.trayectago.app.ui.componentes.IndicadorPasos
import co.trayectago.app.ui.componentes.SelectorDigito

// Medidas del prototipo (Main.dc.html) que no son token: tarjeta de 88 dp, bordes de 2 y 1 dp y raya de 6 dp con hueco de 4 dp.
private val ALTO_OPCION = 88.dp
private val BORDE_SELECCION = 2.dp
private val BORDE_TARJETA = 1.dp
private val RAYA = 6.dp
private val HUECO = 4.dp

/** Pantalla 1: tipo de vehículo, último dígito de la placa y parrillero. */
@Composable
fun PantallaVehiculo(pantalla: Pantalla.Vehiculo, alGuardar: (Int, Boolean) -> Unit) {
    var digito by rememberSaveable { mutableStateOf<Int?>(null) }
    var parrillero by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = Espacios.space5, end = Espacios.space5, top = Espacios.space8, bottom = Espacios.space8),
        verticalArrangement = Arrangement.spacedBy(Espacios.space6),
    ) {
        IndicadorPasos(1)
        Column(verticalArrangement = Arrangement.spacedBy(Espacios.space2)) {
            Text(Textos.VEHICULO_TITULO, style = Trayecta.estilos.titleXl)
            Text(Textos.VEHICULO_SUBTITULO, style = Trayecta.estilos.body, color = Trayecta.colores.inkMuted)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Espacios.space3)) {
            Text(Textos.VEHICULO_SECCION, style = Trayecta.estilos.label, color = Trayecta.colores.inkMuted)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(Espacios.space3)) {
                OpcionVehiculo(Textos.VEHICULO_MOTO, Modifier.weight(1f), seleccionada = true)
                OpcionVehiculo(Textos.VEHICULO_CARRO, Modifier.weight(1f), detalle = Textos.PROXIMAMENTE)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Espacios.space3)) {
            Text(Textos.DIGITO_SECCION, style = Trayecta.estilos.label, color = Trayecta.colores.inkMuted)
            SelectorDigito(elegido = digito, alElegir = { digito = it })
            Text(Textos.DIGITO_AYUDA, style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted)
        }
        FilaParrillero(parrillero) { parrillero = it }
        pantalla.error?.let {
            Text(it, style = Trayecta.estilos.body, color = Trayecta.colores.critical, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        BotonPrincipal(
            Textos.CONTINUAR,
            alPulsar = { digito?.let { alGuardar(it, parrillero) } },
            habilitado = digito != null && !pantalla.guardando,
        )
    }
}

/** Moto (elegida: fondo de marca y borde de 2 dp) o Carro (deshabilitada: borde discontinuo). */
@Composable
private fun OpcionVehiculo(texto: String, modifier: Modifier, seleccionada: Boolean = false, detalle: String? = null) {
    val forma = RoundedCornerShape(Radios.radiusLg)
    val colores = Trayecta.colores
    val estilo = if (seleccionada) {
        Modifier
            .background(colores.brandTint, forma)
            .border(BORDE_SELECCION, colores.brand, forma)
            .clip(forma)
            .selectable(selected = true, role = Role.RadioButton, onClick = {})
    } else {
        // Compose no trae bordes discontinuos: se trazan a mano, hacia adentro para que no se recorten.
        Modifier
            .background(colores.surfaceRaised, forma)
            .drawBehind {
                val trazo = BORDE_CONTROL.toPx()
                drawRoundRect(
                    color = colores.lineStrong,
                    topLeft = Offset(trazo / 2, trazo / 2),
                    size = Size(size.width - trazo, size.height - trazo),
                    cornerRadius = CornerRadius(Radios.radiusLg.toPx()),
                    style = Stroke(trazo, pathEffect = PathEffect.dashPathEffect(floatArrayOf(RAYA.toPx(), HUECO.toPx()))),
                )
            }
            .semantics(mergeDescendants = true) { disabled() }
    }
    Column(
        modifier.heightIn(min = ALTO_OPCION).then(estilo).padding(Espacios.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(texto, style = Trayecta.estilos.button, color = if (seleccionada) colores.brandTintInk else colores.inkMuted)
        detalle?.let { Text(it, style = Trayecta.estilos.caption, color = colores.inkMuted) }
    }
}

/** Toda la fila es el interruptor: TalkBack lee el texto junto con el estado. */
@Composable
private fun FilaParrillero(activo: Boolean, alCambiar: (Boolean) -> Unit) {
    val forma = RoundedCornerShape(Radios.radiusMd)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Tamanos.touchMin)
            .clip(forma)
            .border(BORDE_TARJETA, Trayecta.colores.line, forma)
            .toggleable(value = activo, role = Role.Switch, onValueChange = alCambiar)
            .padding(horizontal = Espacios.space4, vertical = Espacios.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacios.space3),
    ) {
        Column(Modifier.weight(1f)) {
            Text(Textos.PARRILLERO_TITULO, style = Trayecta.estilos.body)
            Text(Textos.PARRILLERO_AYUDA, style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted)
        }
        Switch(checked = activo, onCheckedChange = null)
    }
}
