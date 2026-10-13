package pe.misfinanzas.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Claro = lightColorScheme(
    primary = Color(0xFF1F5F8B), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E5F3), onPrimaryContainer = Color(0xFF0B2A40),
    secondary = Color(0xFF1B7A52), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5EFE2), onSecondaryContainer = Color(0xFF0A2E1F),
    error = Color(0xFFC2482F),
    background = Color(0xFFF3F5F4), onBackground = Color(0xFF15211D),
    surface = Color(0xFFF3F5F4), onSurface = Color(0xFF15211D),
    surfaceVariant = Color(0xFFDDE4E1), onSurfaceVariant = Color(0xFF5D6B66),
    outline = Color(0xFFB9C4C0), outlineVariant = Color(0xFFDBE2DF),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEDF1EF), surfaceContainerHigh = Color(0xFFE7ECEA),
    surfaceContainerHighest = Color(0xFFE1E7E4),
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFF8CC2E8), onPrimary = Color(0xFF07304A),
    primaryContainer = Color(0xFF1A4766), onPrimaryContainer = Color(0xFFD3E5F3),
    secondary = Color(0xFF4CC48D), onSecondary = Color(0xFF00391F),
    secondaryContainer = Color(0xFF173A2B), onSecondaryContainer = Color(0xFFD5EFE2),
    error = Color(0xFFF08B72),
    background = Color(0xFF0F1513), onBackground = Color(0xFFE6EEEB),
    surface = Color(0xFF0F1513), onSurface = Color(0xFFE6EEEB),
    surfaceVariant = Color(0xFF2A3632), onSurfaceVariant = Color(0xFF93A39D),
    outline = Color(0xFF52605B), outlineVariant = Color(0xFF2A3632),
    surfaceContainerLowest = Color(0xFF0A0F0D), surfaceContainerLow = Color(0xFF17201D),
    surfaceContainer = Color(0xFF1B2421), surfaceContainerHigh = Color(0xFF222C29),
    surfaceContainerHighest = Color(0xFF2A3431),
)

private val Tipografia = Typography().let { t ->
    t.copy(
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = t.labelSmall.copy(letterSpacing = 0.8.sp),
    )
}

/** Estilo para montos: cifras de ancho fijo para que se alineen. */
val EstiloMonto = TextStyle(fontFamily = FontFamily.Monospace, fontFeatureSettings = "tnum")

object ColoresFinanzas {
    val ingreso: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4CC48D) else Color(0xFF1B7A52)
    val gasto: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF08B72) else Color(0xFFC2482F)
    val alerta: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE2B04A) else Color(0xFFB07A12)
}

@Composable
fun TemaFinanzas(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        typography = Tipografia,
        content = contenido,
    )
}
