// Generado por diseno/generar-tema.mjs desde diseno/tokens.json. No editar a mano.
package co.trayectago.app.tema

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class ColoresTrayecta(
    val brand: Color,
    val brandPressed: Color,
    val brandTint: Color,
    val brandTintInk: Color,
    val onBrand: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val mapGround: Color,
    val ink: Color,
    val inkMuted: Color,
    val line: Color,
    val lineStrong: Color,
    val alert: Color,
    val onAlert: Color,
    val alertTint: Color,
    val alertStrong: Color,
    val critical: Color,
    val focus: Color,
    val trafficFree: Color,
    val trafficSlow: Color,
    val trafficJam: Color,
    val trafficMark: Color,
    val routeCasing: Color,
    val mapWater: Color,
    val mapPark: Color,
    val mapBuilding: Color,
    val mapRoadMajor: Color,
    val mapRoadMinor: Color,
    val mapRoadCasing: Color,
    val mapRoadRestricted: Color,
    val mapLabel: Color,
    val mapLabelStrong: Color,
    val mapZonePicoPlaca: Color,
)

val ColoresDia = ColoresTrayecta(
    brand = Color(0xFF0F766E),
    brandPressed = Color(0xFF004D40),
    brandTint = Color(0xFFDFF3EF),
    brandTintInk = Color(0xFF004D40),
    onBrand = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFF6F8F7),
    mapGround = Color(0xFFEEF2F1),
    ink = Color(0xFF14161A),
    inkMuted = Color(0xFF5F6368),
    line = Color(0xFFDCE5E2),
    lineStrong = Color(0xFF8A9490),
    alert = Color(0xFFFFB020),
    onAlert = Color(0xFF14161A),
    alertTint = Color(0xFFFFF4DC),
    alertStrong = Color(0xFFB45309),
    critical = Color(0xFFB42318),
    focus = Color(0xFF004D40),
    trafficFree = Color(0xFF0F766E),
    trafficSlow = Color(0xFF0F766E),
    trafficJam = Color(0xFF14161A),
    trafficMark = Color(0xFFFFFFFF),
    routeCasing = Color(0xFFFFFFFF),
    mapWater = Color(0xFFBCDCEB),
    mapPark = Color(0xFFD7EBDF),
    mapBuilding = Color(0xFFE2E8E6),
    mapRoadMajor = Color(0xFFFFFFFF),
    mapRoadMinor = Color(0xFFF8FAF9),
    mapRoadCasing = Color(0xFFC9D3D0),
    mapRoadRestricted = Color(0xFF8A9490),
    mapLabel = Color(0xFF5F6368),
    mapLabelStrong = Color(0xFF14161A),
    mapZonePicoPlaca = Color(0xFF5F6368),
)

val ColoresNoche = ColoresTrayecta(
    brand = Color(0xFF2DD4BF),
    brandPressed = Color(0xFF5EEAD4),
    brandTint = Color(0xFF0F2E2B),
    brandTintInk = Color(0xFF99F6E4),
    onBrand = Color(0xFF14161A),
    surface = Color(0xFF14161A),
    surfaceRaised = Color(0xFF1E2127),
    mapGround = Color(0xFF1A1D22),
    ink = Color(0xFFF5F5F5),
    inkMuted = Color(0xFF9AA0A6),
    line = Color(0xFF2E323A),
    lineStrong = Color(0xFF7C838B),
    alert = Color(0xFFFFB020),
    onAlert = Color(0xFF14161A),
    alertTint = Color(0xFF3A2E12),
    alertStrong = Color(0xFFFFB020),
    critical = Color(0xFFFDA29B),
    focus = Color(0xFF99F6E4),
    trafficFree = Color(0xFF2DD4BF),
    trafficSlow = Color(0xFF2DD4BF),
    trafficJam = Color(0xFFF5F5F5),
    trafficMark = Color(0xFF14161A),
    routeCasing = Color(0xFF14161A),
    mapWater = Color(0xFF1B3442),
    mapPark = Color(0xFF1C2A24),
    mapBuilding = Color(0xFF23272D),
    mapRoadMajor = Color(0xFF3A4048),
    mapRoadMinor = Color(0xFF2A2F36),
    mapRoadCasing = Color(0xFF20242A),
    mapRoadRestricted = Color(0xFF7C838B),
    mapLabel = Color(0xFF9AA0A6),
    mapLabelStrong = Color(0xFFF5F5F5),
    mapZonePicoPlaca = Color(0xFF9AA0A6),
)

object Espacios {
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space12 = 48.dp
}

object Radios {
    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 22.dp
    val radiusFull = 9999.dp
}

object Tamanos {
    val touchMin = 52.dp
    val touchLg = 64.dp
    val fab = 72.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val strokeIcon = 2.dp
}

@Immutable
data class EstilosTexto(
    val navDistance: TextStyle,
    val titleXl: TextStyle,
    val titleLg: TextStyle,
    val titleMd: TextStyle,
    val button: TextStyle,
    val bodyLg: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val label: TextStyle,
)

fun estilosTexto(display: FontFamily, texto: FontFamily) = EstilosTexto(
    navDistance = TextStyle(fontFamily = display, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight(800)),
    titleXl = TextStyle(fontFamily = display, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight(800)),
    titleLg = TextStyle(fontFamily = display, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight(700)),
    titleMd = TextStyle(fontFamily = display, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight(700)),
    button = TextStyle(fontFamily = display, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight(700)),
    bodyLg = TextStyle(fontFamily = texto, fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight(400)),
    body = TextStyle(fontFamily = texto, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight(400)),
    caption = TextStyle(fontFamily = texto, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight(400)),
    label = TextStyle(fontFamily = texto, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(600), letterSpacing = 0.4.sp),
)
