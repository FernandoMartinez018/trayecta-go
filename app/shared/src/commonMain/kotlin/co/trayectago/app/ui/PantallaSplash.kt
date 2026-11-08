package co.trayectago.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.componentes.SimboloCronometroT

// Medidas del logo en el prototipo (Splash.dc.html); no hay tokens para el logotipo.
private val SIMBOLO_SPLASH = 132.dp
private val LOGOTIPO_TAMANO = 36.sp
private val LOGOTIPO_INTERLINEADO = 44.sp

/** Pantalla 0: marca sobre fondo brand mientras se revisa la sesión. */
@Composable
fun PantallaSplash() {
    Column(
        Modifier.fillMaxSize().background(Trayecta.colores.brand).safeDrawingPadding().padding(Espacios.space8),
        verticalArrangement = Arrangement.spacedBy(Espacios.space4, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SimboloCronometroT(Trayecta.colores.onBrand, SIMBOLO_SPLASH)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(Textos.LOGO_TRAYECTA) }
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(Textos.LOGO_GO) }
            },
            style = Trayecta.estilos.titleXl.copy(fontSize = LOGOTIPO_TAMANO, lineHeight = LOGOTIPO_INTERLINEADO),
            color = Trayecta.colores.onBrand,
        )
        Text(Textos.ESLOGAN, style = Trayecta.estilos.bodyLg, color = Trayecta.colores.brandTint)
    }
}
