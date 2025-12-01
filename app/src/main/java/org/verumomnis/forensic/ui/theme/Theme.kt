package org.verumomnis.forensic.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Verum Omnis Brand Colors
private val VerumBlue = Color(0xFF1A5276)
private val VerumGold = Color(0xFFC9A227)
private val VerumDark = Color(0xFF1C2833)
private val VerumLight = Color(0xFFF8F9F9)

private val DarkColorScheme = darkColorScheme(
    primary = VerumGold,
    secondary = VerumBlue,
    tertiary = Color(0xFF58D68D),
    background = VerumDark,
    surface = Color(0xFF212F3D),
    onPrimary = VerumDark,
    onSecondary = VerumLight,
    onTertiary = VerumDark,
    onBackground = VerumLight,
    onSurface = VerumLight
)

private val LightColorScheme = lightColorScheme(
    primary = VerumBlue,
    secondary = VerumGold,
    tertiary = Color(0xFF27AE60),
    background = VerumLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = VerumDark,
    onTertiary = Color.White,
    onBackground = VerumDark,
    onSurface = VerumDark
)

@Composable
fun VerumOmnisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
