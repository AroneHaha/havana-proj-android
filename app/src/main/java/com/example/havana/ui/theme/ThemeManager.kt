package com.example.havana.ui.theme

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.havana.data.session.SessionManager

/**
 * Centralized theme manager for the Havana app.
 *
 * Responsibilities:
 *   - Persists the user's dark-mode preference via [SessionManager].
 *   - Provides a reactive [isDarkMode] state that Compose can observe.
 *   - Bridges between Compose theme and AppCompatDelegate so that
 *     configuration-aware resources (e.g. values-night/) also respect
 *     the user's choice.
 *   - Prevents theme flicker on cold start by applying the saved
 *     preference before the first frame is drawn.
 *
 * Usage in Activity.onCreate (before setContent):
 *   ThemeManager.applySavedTheme()
 *
 * Usage inside Compose:
 *   val isDark by ThemeManager.isDarkModeState.collectAsState()
 *   HavanaTheme(darkTheme = isDark) { ... }
 */
object ThemeManager {

    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: Boolean get() = _isDarkMode.value

    /** Apply the persisted theme preference immediately (call in Activity.onCreate before setContent). */
    fun applySavedTheme() {
        val dark = SessionManager.isDarkMode
        _isDarkMode.value = dark
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /** Toggle between light and dark mode. Persists the choice and updates AppCompat. */
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        SessionManager.setDarkMode(enabled)
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /** Convenience: toggle to the opposite of the current mode. */
    fun toggle() = setDarkMode(!_isDarkMode.value)

    /**
     * Compose utility that keeps the Activity's status-bar appearance
     * in sync with the current theme.  Call once at the top level of
     * your Compose tree.
     */
    @Composable
    fun ObserveThemeForStatusBar() {
        val context = LocalContext.current
        val dark = isDarkMode

        DisposableEffect(dark) {
            val activity = context as? Activity
            activity?.window?.let { window ->
                val decor = window.decorView
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, decor)
                insetsController.isAppearanceLightStatusBars = !dark
            }
            onDispose { /* no-op */ }
        }
    }
}