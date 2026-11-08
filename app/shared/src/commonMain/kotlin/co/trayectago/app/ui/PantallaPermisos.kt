package co.trayectago.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.trayectago.app.estado.Pantalla
import co.trayectago.app.permisos.Permiso
import co.trayectago.app.shared.resources.Res
import co.trayectago.app.shared.resources.ic_microfono
import co.trayectago.app.shared.resources.ic_notificaciones
import co.trayectago.app.shared.resources.ic_reloj
import co.trayectago.app.shared.resources.ic_ubicacion
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.componentes.BotonPrincipal
import co.trayectago.app.ui.componentes.BotonTexto
import co.trayectago.app.ui.componentes.IndicadorPasos
import co.trayectago.app.ui.componentes.bordeDiscontinuo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// Medidas del prototipo (Permisos.dc.html) que no son token: caja del ícono de 40 dp y borde de tarjeta de 1 dp.
private val CUADRO_ICONO = 40.dp
private val BORDE_TARJETA = 1.dp

/** Pantalla 2: permisos graduales. «Continuar» y «Ahora no» llevan a Inicio con o sin permisos. */
@Composable
fun PantallaPermisos(pantalla: Pantalla.Permisos, alPermitir: (Permiso) -> Unit, alContinuar: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(start = Espacios.space5, end = Espacios.space5, top = Espacios.space8, bottom = Espacios.space5),
        verticalArrangement = Arrangement.spacedBy(Espacios.space5),
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Espacios.space5),
        ) {
            IndicadorPasos(2)
            Column(verticalArrangement = Arrangement.spacedBy(Espacios.space2)) {
                Text(Textos.PERMISOS_TITULO, style = Trayecta.estilos.titleXl)
                Text(Textos.PERMISOS_SUBTITULO, style = Trayecta.estilos.body, color = Trayecta.colores.inkMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espacios.space3)) {
                TarjetaPermiso(Res.drawable.ic_ubicacion, Textos.PERMISO_UBICACION, Textos.PERMISO_UBICACION_AYUDA, Permiso.UBICACION in pantalla.concedidos) {
                    alPermitir(Permiso.UBICACION)
                }
                TarjetaPermiso(Res.drawable.ic_notificaciones, Textos.PERMISO_NOTIFICACIONES, Textos.PERMISO_NOTIFICACIONES_AYUDA, Permiso.NOTIFICACIONES in pantalla.concedidos) {
                    alPermitir(Permiso.NOTIFICACIONES)
                }
                TarjetaPermiso(Res.drawable.ic_microfono, Textos.PERMISO_MICROFONO, Textos.PERMISO_MICROFONO_AYUDA, Permiso.MICROFONO in pantalla.concedidos) {
                    alPermitir(Permiso.MICROFONO)
                }
                TarjetaSegundoPlano()
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(Espacios.space2)) {
            BotonPrincipal(Textos.CONTINUAR, alContinuar)
            BotonTexto(Textos.AHORA_NO, alContinuar, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TarjetaPermiso(icono: DrawableResource, titulo: String, ayuda: String, concedido: Boolean, alPermitir: () -> Unit) {
    val colores = Trayecta.colores
    Row(
        Modifier
            .fillMaxWidth()
            .border(BORDE_TARJETA, colores.line, RoundedCornerShape(Radios.radiusMd))
            .padding(horizontal = Espacios.space4, vertical = Espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacios.space3),
    ) {
        CuadroIcono(icono, fondo = colores.brandTint, tinte = colores.brandTintInk)
        TituloYAyuda(titulo, ayuda, Modifier.weight(1f))
        Button(
            onClick = alPermitir,
            enabled = !concedido,
            modifier = Modifier
                .heightIn(min = Tamanos.touchMin)
                .semantics { contentDescription = if (concedido) Textos.permitido(titulo) else Textos.permitir(titulo) },
            shape = RoundedCornerShape(Radios.radiusMd),
            contentPadding = PaddingValues(horizontal = Espacios.space4),
            colors = ButtonDefaults.buttonColors(
                containerColor = colores.brand,
                contentColor = colores.onBrand,
                disabledContainerColor = colores.brandTint,
                disabledContentColor = colores.brandTintInk,
            ),
        ) {
            Text(if (concedido) Textos.PERMITIDO else Textos.PERMITIR, style = Trayecta.estilos.button)
        }
    }
}

/** Informativa, sin botón: la ubicación en segundo plano solo se pide al activar «Aprender mis rutas». */
@Composable
private fun TarjetaSegundoPlano() {
    val colores = Trayecta.colores
    Row(
        Modifier
            .fillMaxWidth()
            .background(colores.surfaceRaised, RoundedCornerShape(Radios.radiusMd))
            .bordeDiscontinuo(colores.lineStrong, Radios.radiusMd)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = Espacios.space4, vertical = Espacios.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Espacios.space3),
    ) {
        CuadroIcono(Res.drawable.ic_reloj, fondo = colores.surface, tinte = colores.inkMuted)
        TituloYAyuda(Textos.PERMISO_SEGUNDO_PLANO, Textos.PERMISO_SEGUNDO_PLANO_AYUDA, Modifier.weight(1f))
    }
}

@Composable
private fun CuadroIcono(icono: DrawableResource, fondo: Color, tinte: Color) {
    Box(Modifier.size(CUADRO_ICONO).background(fondo, RoundedCornerShape(Radios.radiusMd)), contentAlignment = Alignment.Center) {
        Icon(painterResource(icono), contentDescription = null, tint = tinte, modifier = Modifier.size(Tamanos.iconMd))
    }
}

@Composable
private fun TituloYAyuda(titulo: String, ayuda: String, modifier: Modifier) {
    Column(modifier) {
        Text(titulo, style = Trayecta.estilos.body.copy(fontWeight = FontWeight.SemiBold))
        Text(ayuda, style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted)
    }
}
