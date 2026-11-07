package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF001B24),
    primaryContainer = Color(0xFF003845),
    onPrimaryContainer = Color(0xFF99F3FF),
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00368A),
    onSecondaryContainer = Color(0xFFB5D4FF),
    tertiary = JarvisNeonMint,
    onTertiary = Color(0xFF00291B),
    tertiaryContainer = Color(0xFF004D36),
    onTertiaryContainer = Color(0xFF96FDD6),
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisBorder,
    error = JarvisWarningRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve JARVIS iconic dark navy & electric cyan styling
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
