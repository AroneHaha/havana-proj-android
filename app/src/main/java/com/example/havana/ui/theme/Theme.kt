package com.example.havana.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
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
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Error,
    onError = Color.White,
)

/**
 * Havana's root theme composable.
 *
 * @param darkTheme  Whether to use the dark colour scheme.
 *                   Defaults to the value persisted in [ThemeManager],
 *                   falling back to the system setting on first launch.
 * @param content    The composable subtree.
 */
@Composable
fun HavanaTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Keep the Activity's status bar in sync with the theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Also sync via ThemeManager (covers navigation bar, etc.)
    ThemeManager.ObserveThemeForStatusBar()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HavanaTypography,
        content = content,
    )
}