package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.Color

private val FactbookDarkColorScheme = darkColorScheme(
    primary = IntelGold,
    onPrimary = Color.White,
    primaryContainer = IntelGoldLight,
    onPrimaryContainer = IntelOnSurface,
    secondary = IntelSecondary,
    onSecondary = Color.White,
    background = IntelDarkBg,
    onBackground = IntelOnSurface,
    surface = IntelSurface,
    onSurface = IntelOnSurface,
    surfaceVariant = IntelSurfaceVariant,
    onSurfaceVariant = IntelOnBg,
    error = IntelAlert,
    onError = Color.White,
    outline = IntelMuted
)

private val FactbookLightColorScheme = lightColorScheme(
    primary = IntelGold,
    onPrimary = Color.White,
    primaryContainer = IntelGoldLight,
    onPrimaryContainer = IntelOnSurface,
    secondary = IntelSecondary,
    onSecondary = Color.White,
    background = IntelDarkBg,
    onBackground = IntelOnSurface,
    surface = IntelSurface,
    onSurface = IntelOnSurface,
    surfaceVariant = IntelSurfaceVariant,
    onSurfaceVariant = IntelOnBg,
    error = IntelAlert,
    onError = Color.White,
    outline = IntelMuted
)

@Composable
fun FactbookTheme(
    darkTheme: Boolean = true, // We default to dark theme for that immersive "Mission Briefing Room" vibe
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FactbookDarkColorScheme else FactbookLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
