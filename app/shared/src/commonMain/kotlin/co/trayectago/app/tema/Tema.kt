package co.trayectago.app.tema

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import co.trayectago.app.shared.resources.Res
import co.trayectago.app.shared.resources.plus_jakarta_sans_bold
import co.trayectago.app.shared.resources.plus_jakarta_sans_extrabold
import org.jetbrains.compose.resources.Font

private val LocalColores = staticCompositionLocalOf { ColoresDia }
private val LocalEstilos = staticCompositionLocalOf<EstilosTexto> { error("Envuelve la interfaz en TemaTrayecta") }

/** Tema de Trayecta Go: tokens de diseno/tokens.json (vía TemaGenerado.kt), día y noche. */
@Composable
fun TemaTrayecta(oscuro: Boolean = isSystemInDarkTheme(), contenido: @Composable () -> Unit) {
    val colores = if (oscuro) ColoresNoche else ColoresDia
    // Solo 700 y 800: Plus Jakarta Sans es para títulos, cifras y botones; el cuerpo usa la fuente del sistema.
    val display = FontFamily(
        Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
        Font(Res.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
    )
    val estilos = remember(display) { estilosTexto(display, FontFamily.Default) }

    CompositionLocalProvider(LocalColores provides colores, LocalEstilos provides estilos) {
        MaterialTheme(colorScheme = esquemaMaterial(colores, oscuro), content = contenido)
    }
}

private fun esquemaMaterial(c: ColoresTrayecta, oscuro: Boolean): ColorScheme {
    val base = if (oscuro) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = c.brand,
        onPrimary = c.onBrand,
        primaryContainer = c.brandTint,
        onPrimaryContainer = c.brandTintInk,
        background = c.surface,
        onBackground = c.ink,
        surface = c.surface,
        onSurface = c.ink,
        surfaceVariant = c.surfaceRaised,
        onSurfaceVariant = c.inkMuted,
        outline = c.lineStrong,
        outlineVariant = c.line,
        error = c.critical,
    )
}

object Trayecta {
    val colores: ColoresTrayecta
        @Composable @ReadOnlyComposable get() = LocalColores.current

    val estilos: EstilosTexto
        @Composable @ReadOnlyComposable get() = LocalEstilos.current
}
