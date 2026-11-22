package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PenguinPlusColorScheme = lightColorScheme(
    primary = BurgundyHeader,
    onPrimary = Color.White,
    primaryContainer = BurgundyAccent,
    onPrimaryContainer = Color.White,
    secondary = AmberPro,
    onSecondary = Color.Black,
    secondaryContainer = DarkContainer,
    onSecondaryContainer = Color.White,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = CreamBackground,
    onBackground = TextPrimaryDark,
    surface = CreamSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkContainer,
    onSurfaceVariant = TextPrimaryLight,
    outline = DarkCardBorder,
    error = CoralError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PenguinPlusColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BurgundyHeader.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
