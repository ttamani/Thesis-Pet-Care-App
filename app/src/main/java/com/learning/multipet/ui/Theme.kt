package com.learning.multipet.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    // Shared semantic tokens
    val AccentPrimary = Color(0xFF14B8A6)
    val AccentPressed = Color(0xFF0F9E90)

    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Danger = Color(0xFFEF4444)
}

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F9E90),
    onPrimary = Color.White,

    secondary = Color(0xFF14B8A6),
    onSecondary = Color.White,

    background = Color(0xFFF6F8FB),
    onBackground = Color(0xFF0F172A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFE9EEF5),
    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFFD5DDE8),
    outlineVariant = Color(0xFFE2E8F0),

    error = AppColors.Danger,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF14B8A6),
    onPrimary = Color.White,

    secondary = Color(0xFF14B8A6),
    onSecondary = Color.White,

    background = Color(0xFF0B1220),
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF162033),
    onSurface = Color(0xFFF8FAFC),

    surfaceVariant = Color(0xFF1A263B),
    onSurfaceVariant = Color(0xFFD6DEEA),

    outline = Color(0xFF2A3850),
    outlineVariant = Color(0xFF39506E),

    error = AppColors.Danger,
    onError = Color.White
)

@Composable
fun AppTheme(
    themePreference: ThemePreference = ThemePreference.LIGHT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val useDark = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemDark
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkScheme else LightScheme,
        content = content
    )
}
