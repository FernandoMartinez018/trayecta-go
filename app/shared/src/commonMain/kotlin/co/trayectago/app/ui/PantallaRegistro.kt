package co.trayectago.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.trayectago.app.auth.OpcionIngreso
import co.trayectago.app.ingreso.correoValido
import co.trayectago.app.tema.Espacios
import co.trayectago.app.tema.Radios
import co.trayectago.app.tema.Tamanos
import co.trayectago.app.tema.Trayecta
import co.trayectago.app.ui.componentes.BORDE_CONTROL
import co.trayectago.app.ui.componentes.BotonPrincipal
import co.trayectago.app.ui.componentes.BotonSecundario
import co.trayectago.app.ui.componentes.BotonTexto
import co.trayectago.app.ui.componentes.SimboloCronometroT

// Medidas del logo en el prototipo (Login.dc.html): cuadro de 88 dp con el símbolo de 64 dp.
private val CUADRO_LOGO = 88.dp
private val SIMBOLO_REGISTRO = 64.dp

/** Pantalla 12: registro e inicio de sesión con Google o con un enlace al correo. */
@Composable
fun PantallaRegistro(error: String?, alIngresar: (OpcionIngreso) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    val puedeEnviar = correoValido(correo)
    val enviar = { if (puedeEnviar) alIngresar(OpcionIngreso.Correo(correo)) }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = Espacios.space5, end = Espacios.space5, top = Espacios.space12, bottom = Espacios.space8),
        verticalArrangement = Arrangement.spacedBy(Espacios.space6),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Espacios.space3)) {
            Box(
                Modifier.size(CUADRO_LOGO).background(Trayecta.colores.brand, RoundedCornerShape(Radios.radiusXl)),
                contentAlignment = Alignment.Center,
            ) {
                SimboloCronometroT(Trayecta.colores.onBrand, SIMBOLO_REGISTRO)
            }
            Text(Textos.REGISTRO_TITULO, style = Trayecta.estilos.titleXl, color = Trayecta.colores.ink)
            Text(Textos.REGISTRO_SUBTITULO, style = Trayecta.estilos.body, color = Trayecta.colores.inkMuted, textAlign = TextAlign.Center)
        }

        Column(verticalArrangement = Arrangement.spacedBy(Espacios.space3)) {
            BotonSecundario(Textos.CONTINUAR_GOOGLE, alPulsar = { alIngresar(OpcionIngreso.Google) })

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Espacios.space3)) {
                HorizontalDivider(Modifier.weight(1f), color = Trayecta.colores.line)
                Text(Textos.O_CON_CORREO, style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted)
                HorizontalDivider(Modifier.weight(1f), color = Trayecta.colores.line)
            }

            Text(Textos.CORREO_ETIQUETA, style = Trayecta.estilos.body.copy(fontWeight = FontWeight.SemiBold), color = Trayecta.colores.ink)
            BasicTextField(
                value = correo,
                onValueChange = { correo = it },
                singleLine = true,
                textStyle = Trayecta.estilos.bodyLg.copy(color = Trayecta.colores.ink),
                cursorBrush = SolidColor(Trayecta.colores.brand),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { enviar() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Tamanos.touchMin)
                    .semantics { contentDescription = Textos.CORREO_ETIQUETA },
                decorationBox = { campo ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = Tamanos.touchMin)
                            .border(BORDE_CONTROL, Trayecta.colores.lineStrong, RoundedCornerShape(Radios.radiusMd))
                            .padding(horizontal = Espacios.space4),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (correo.isEmpty()) {
                            Text(Textos.CORREO_MARCADOR, style = Trayecta.estilos.bodyLg, color = Trayecta.colores.inkMuted)
                        }
                        campo()
                    }
                },
            )
            BotonPrincipal(Textos.ENVIARME_ENLACE, alPulsar = enviar, habilitado = puedeEnviar)
            Text(
                Textos.ENLACE_AYUDA,
                style = Trayecta.estilos.caption,
                color = Trayecta.colores.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Text(error, style = Trayecta.estilos.body, color = Trayecta.colores.critical, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(Espacios.space8))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(Textos.TERMINOS, style = Trayecta.estilos.caption, color = Trayecta.colores.inkMuted, textAlign = TextAlign.Center)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Textos.YA_TIENES_CUENTA, style = Trayecta.estilos.body, color = Trayecta.colores.ink)
                BotonTexto(Textos.INICIA_SESION, alPulsar = { alIngresar(OpcionIngreso.General) })
            }
        }
    }
}
