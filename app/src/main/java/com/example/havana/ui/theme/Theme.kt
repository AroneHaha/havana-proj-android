package com.example.havana.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Maroon,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color.White,
    tertiary = GoldLight,
    background = CreamBg,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    error = Error,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    secondary = MaroonLight,
    onSecondary = Color.White,
    tertiary = GoldDark,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    error = Error,
    onError = Color.White,
)

@Composable
fun HavanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HavanaTypography,
        content = content,
    )
}