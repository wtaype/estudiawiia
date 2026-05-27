package com.estudiawii.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.estudiawii.app.R

data class WiThemeColors(
    val name: String,
    val bg: Color,
    val wb: Color,
    val tx: Color,
    val tx1: Color,
    val tx2: Color,
    val tx3: Color,
    val txa: Color,
    val txe: Color,
    val hv: Color,
    val hva: Color,
    val mco: Color,
    val mbg: Color,
    val brd: Color,
    val inp: Color,
    val isDark: Boolean = false,
)

val PazThemeColors = WiThemeColors(
    name = "Paz",
    bg = Color(0xFFCCFFCE),
    wb = Color(0xFFEBFFEB),
    tx = Color(0xFF000000),
    tx1 = Color(0xFF001A00),
    tx2 = Color(0xFF003300),
    tx3 = Color(0xFF006600),
    txa = Color(0xFFFFFFFF),
    txe = Color(0xFF000000),
    hv = Color(0xFF3CD741),
    hva = Color(0xFF25B62A),
    mco = Color(0xFF25B62A),
    mbg = Color(0xFF25B62A),
    brd = Color(0xFFA8E6AB),
    inp = Color(0xFFF0FFF1),
)

val CieloThemeColors = WiThemeColors(
    name = "Cielo",
    bg = Color(0xFFCCEFFF),
    wb = Color(0xFFE5F7FF),
    tx = Color(0xFF000000),
    tx1 = Color(0xFF1A1A1A),
    tx2 = Color(0xFF333333),
    tx3 = Color(0xFF666666),
    txa = Color(0xFFFFFFFF),
    txe = Color(0xFF000000),
    hv = Color(0xFF00A8E6),
    hva = Color(0xFF1873CD),
    mco = Color(0xFF1978D7),
    mbg = Color(0xFF1978D7),
    brd = Color(0xFFB8D9EB),
    inp = Color(0xFFF0F9FF),
)

val DulceThemeColors = WiThemeColors(
    name = "Dulce",
    bg = Color(0xFFFFCCD1),
    wb = Color(0xFFFFEBED),
    tx = Color(0xFF000000),
    tx1 = Color(0xFF1A0000),
    tx2 = Color(0xFF330000),
    tx3 = Color(0xFF660000),
    txa = Color(0xFFFFFFFF),
    txe = Color(0xFF000000),
    hv = Color(0xFFFF7A85),
    hva = Color(0xFFFF3849),
    mco = Color(0xFFFF3849),
    mbg = Color(0xFFFF3849),
    brd = Color(0xFFFFB3BA),
    inp = Color(0xFFFFF5F6),
)

val OroThemeColors = WiThemeColors(
    name = "Oro",
    bg = Color(0xFFFFF8D1),
    wb = Color(0xFFFFFDE8),
    tx = Color(0xFF000000),
    tx1 = Color(0xFF1A1500),
    tx2 = Color(0xFF332B00),
    tx3 = Color(0xFF665500),
    txa = Color(0xFF000000),
    txe = Color(0xFF000000),
    hv = Color(0xFFF0CC00),
    hva = Color(0xFFC9A800),
    mco = Color(0xFFFFDA34),
    mbg = Color(0xFFFACC00),
    brd = Color(0xFFFFE066),
    inp = Color(0xFFFFFEF5),
)

val MoraThemeColors = WiThemeColors(
    name = "Mora",
    bg = Color(0xFFE4CCFF),
    wb = Color(0xFFF4EBFF),
    tx = Color(0xFF000000),
    tx1 = Color(0xFF1A001A),
    tx2 = Color(0xFF330033),
    tx3 = Color(0xFF660066),
    txa = Color(0xFFFFFFFF),
    txe = Color(0xFF000000),
    hv = Color(0xFF9442FF),
    hva = Color(0xFF5F00DB),
    mco = Color(0xFF6A00F5),
    mbg = Color(0xFF6A00F5),
    brd = Color(0xFFC9A3FF),
    inp = Color(0xFFFAF5FF),
)

val FuturoThemeColors = WiThemeColors(
    name = "Futuro",
    bg = Color(0xFF0A0E1A),
    wb = Color(0xFF151B2E),
    tx = Color(0xFFE0E7FF),
    tx1 = Color(0xFFC7D2FE),
    tx2 = Color(0xFFA5B4FC),
    tx3 = Color(0xFF818CF8),
    txa = Color(0xFF0A0E1A),
    txe = Color(0xFF00F3FF),
    hv = Color(0xFF00D4FF),
    hva = Color(0xFF00F3FF),
    mco = Color(0xFF00F3FF),
    mbg = Color(0xFF151B2E),
    brd = Color(0xFF2D3A52),
    inp = Color(0xFF0F1421),
    isDark = true,
)

val WiThemes = listOf(PazThemeColors, CieloThemeColors, DulceThemeColors, OroThemeColors, MoraThemeColors, FuturoThemeColors)
val LocalWiThemeColors = staticCompositionLocalOf { OroThemeColors }

val WiiFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

val WiiDisplayFontFamily = WiiFontFamily

object WiCss {
    val bg: Color @Composable get() = LocalWiThemeColors.current.bg
    val wb: Color @Composable get() = LocalWiThemeColors.current.wb
    val tx: Color @Composable get() = LocalWiThemeColors.current.tx
    val tx1: Color @Composable get() = LocalWiThemeColors.current.tx1
    val tx2: Color @Composable get() = LocalWiThemeColors.current.tx2
    val tx3: Color @Composable get() = LocalWiThemeColors.current.tx3
    val txa: Color @Composable get() = LocalWiThemeColors.current.txa
    val txe: Color @Composable get() = LocalWiThemeColors.current.txe
    val hv: Color @Composable get() = LocalWiThemeColors.current.hv
    val hva: Color @Composable get() = LocalWiThemeColors.current.hva
    val mco: Color @Composable get() = LocalWiThemeColors.current.mco
    val mbg: Color @Composable get() = LocalWiThemeColors.current.mbg
    val brd: Color @Composable get() = LocalWiThemeColors.current.brd
    val inp: Color @Composable get() = LocalWiThemeColors.current.inp
    val isDark: Boolean @Composable get() = LocalWiThemeColors.current.isDark

    val white = Color.White
    val black = Color(0xFF1A1500)
    val error = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)
    val warning = Color(0xFFFF9800)
    val info = Color(0xFF2196F3)

    val gradPremium: Brush @Composable get() = Brush.linearGradient(
        if (isDark) listOf(bg, wb, bg) else listOf(wb, bg, wb)
    )
    val gradGoldSoft: Brush @Composable get() = Brush.linearGradient(listOf(brd.copy(alpha = 0.8f), mco))
    val padL = PaddingValues(horizontal = 20.dp, vertical = 16.dp)

    fun glassShape(intensity: Float = 0.55f) = RoundedCornerShape(if (intensity > 0.75f) 24.dp else 20.dp)
    @Composable fun glassBorder(intensity: Float = 0.55f) = BorderStroke(1.dp, brd.copy(alpha = 0.40f + intensity * 0.25f))
    @Composable fun glassColors(intensity: Float = 0.55f) = CardDefaults.cardColors(containerColor = wb.copy(alpha = 0.72f + intensity * 0.18f), contentColor = tx1)
    @Composable fun softSurface(alpha: Float = 0.70f): Color = if (isDark) bg.copy(alpha = alpha) else white.copy(alpha = alpha)
    @Composable fun chromeSurface(alpha: Float = 0.98f): Color = wb.copy(alpha = alpha)
}

private val WiiTypography = Typography(
    headlineLarge = TextStyle(fontFamily = WiiDisplayFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = WiiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = WiiFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = WiiFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp),
    labelSmall = TextStyle(fontFamily = WiiFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)

object WiText {
    val display: TextStyle @Composable get() = TextStyle(fontFamily = WiiDisplayFontFamily, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = WiCss.tx1, lineHeight = 40.sp)
    val h1: TextStyle @Composable get() = TextStyle(fontFamily = WiiDisplayFontFamily, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, color = WiCss.tx1, lineHeight = 33.sp)
    val h2: TextStyle @Composable get() = TextStyle(fontFamily = WiiDisplayFontFamily, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, color = WiCss.tx1, lineHeight = 29.sp)
    val h3: TextStyle @Composable get() = TextStyle(fontFamily = WiiDisplayFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = WiCss.tx1, lineHeight = 22.sp)
    val body: TextStyle @Composable get() = TextStyle(fontFamily = WiiFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WiCss.tx2, lineHeight = 22.sp)
    val small: TextStyle @Composable get() = TextStyle(fontFamily = WiiFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = WiCss.tx3, lineHeight = 18.sp)
    val tiny: TextStyle @Composable get() = TextStyle(fontFamily = WiiFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = WiCss.tx3, lineHeight = 15.sp)
    val label: TextStyle @Composable get() = TextStyle(fontFamily = WiiFontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WiCss.tx3)
}

@Composable
fun Modifier.premiumBackground(): Modifier = background(WiCss.gradPremium)

@Composable
fun Modifier.softGlassShadow(): Modifier = shadow(
    elevation = if (WiCss.isDark) 12.dp else 20.dp,
    shape = RoundedCornerShape(20.dp),
    ambientColor = WiCss.mco.copy(alpha = if (WiCss.isDark) 0.18f else 0.10f),
    spotColor = WiCss.mco.copy(alpha = if (WiCss.isDark) 0.12f else 0.18f),
)

@Composable
fun EstudiaWiiTheme(themeColors: WiThemeColors = OroThemeColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalWiThemeColors provides themeColors) {
        val scheme = if (WiCss.isDark) {
            darkColorScheme(primary = WiCss.mco, secondary = WiCss.mco, background = WiCss.bg, surface = WiCss.wb, onSurface = WiCss.tx1)
        } else {
            lightColorScheme(primary = WiCss.mco, secondary = WiCss.mco, background = WiCss.bg, surface = WiCss.wb, onSurface = WiCss.tx1)
        }
        MaterialTheme(colorScheme = scheme, typography = WiiTypography, content = content)
    }
}
