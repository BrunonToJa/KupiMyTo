package pl.kupimyto.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pl.kupimyto.app.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F3D2B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6EBCF),
    onPrimaryContainer = Color(0xFF1F3D2B),
    secondary = Color(0xFF5F7060),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E9CC),
    onSecondaryContainer = Color(0xFF1F3D2B),
    tertiary = Color(0xFFB5651D),
    tertiaryContainer = Color(0xFFE8B4A8),
    onTertiaryContainer = Color(0xFF4A2A22),
    background = Color(0xFFF6F7E7),
    onBackground = Color(0xFF1B2A20),
    surface = Color(0xFFF6F7E7),
    onSurface = Color(0xFF1B2A20),
    surfaceVariant = Color(0xFFE9EED6),
    onSurfaceVariant = Color(0xFF5F7060),
    surfaceContainerLowest = Color(0xFFFBFCF4),
    surfaceContainer = Color(0xFFF8F9EC),
    surfaceContainerHigh = Color(0xFFEEF2DC),
    surfaceContainerHighest = Color(0xFFE9EED6),
    outline = Color(0xFF8A9A86),
    outlineVariant = Color(0xFFDDE2C8),
)

// Ciemna wersja tej samej palety: kremowy tekst, "limonkowy" akcent, zieleń w tle, łososiowy awatar.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFE6EBCF),
    onPrimary = Color(0xFF1F3D2B),
    primaryContainer = Color(0xFF2F4B3A),
    onPrimaryContainer = Color(0xFFE6EBCF),
    secondary = Color(0xFFA9B8A4),
    onSecondary = Color(0xFF1F3D2B),
    secondaryContainer = Color(0xFF38523F),
    onSecondaryContainer = Color(0xFFE6EBCF),
    tertiary = Color(0xFFE8B4A8),
    onTertiary = Color(0xFF4A2A22),
    tertiaryContainer = Color(0xFFE8B4A8),
    onTertiaryContainer = Color(0xFF4A2A22),
    background = Color(0xFF121C16),
    onBackground = Color(0xFFF6F7E7),
    surface = Color(0xFF121C16),
    onSurface = Color(0xFFF6F7E7),
    surfaceVariant = Color(0xFF26382D),
    onSurfaceVariant = Color(0xFFA9B8A4),
    surfaceContainerLowest = Color(0xFF1C2B23),
    surfaceContainerLow = Color(0xFF17231B),
    surfaceContainer = Color(0xFF16221A),
    surfaceContainerHigh = Color(0xFF22322A),
    surfaceContainerHighest = Color(0xFF2A3D32),
    outline = Color(0xFF7B9080),
    outlineVariant = Color(0xFF3F5546),
)

@Composable
fun KupimytoTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Kolor ikon na pasku statusu i nawigacji dopasowany do wybranego motywu
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}