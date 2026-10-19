package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ZenithIndigo,
    onPrimary = Color.White,
    primaryContainer = ZenithSurfaceVariant,
    onPrimaryContainer = ZenithCyan,
    secondary = ZenithViolet,
    onSecondary = Color.White,
    secondaryContainer = ZenithSurfaceSubtle,
    onSecondaryContainer = Color.White,
    tertiary = ZenithCyan,
    onTertiary = Color.Black,
    background = ZenithBgDark,
    onBackground = ZenithTextPrimary,
    surface = ZenithSurface,
    onSurface = ZenithTextPrimary,
    surfaceVariant = ZenithSurfaceVariant,
    onSurfaceVariant = ZenithTextSecondary,
    outline = ZenithProgressTrack
)

@Composable
fun ZenithMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZenithMusicTheme(content = content)
}
