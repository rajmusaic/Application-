package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VibeBlue,
    onPrimary = Color.White,
    primaryContainer = VibeBlueLight,
    onPrimaryContainer = VibeBlueDark,
    secondary = VibeYellow,
    onSecondary = Color(0xFF713F12),
    secondaryContainer = VibeYellowContainer,
    onSecondaryContainer = Color(0xFF854D0E),
    tertiary = VibeBlueDark,
    onTertiary = Color.White,
    background = VibeBackground,
    onBackground = VibeTextPrimary,
    surface = VibeSurface,
    onSurface = VibeTextPrimary,
    surfaceVariant = VibeSurfaceVariant,
    onSurfaceVariant = VibeTextSecondary,
    outline = VibeBorder,
    outlineVariant = VibeBorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = VibeYellow,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF713F12),
    onSecondaryContainer = VibeYellowLight,
    tertiary = Color(0xFF93C5FD),
    onTertiary = Color(0xFF0F172A),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default is Light Mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
