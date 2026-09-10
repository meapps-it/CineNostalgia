package com.meapps.cinenostalgia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

object MEColors {
    val Navy = Color(0xFF0F172A)
    val Blue = Color(0xFF2563EB)
    val Sky = Color(0xFF0EA5E9)
    val Amber = Color(0xFFF59E0B)
    val AmberSoft = Color(0xFFFFF3CD)
    val Green = Color(0xFF16A34A)
    val Red = Color(0xFFDC2626)
    val Background = Color(0xFFF7F9FC)
    val Border = Color(0xFFC9D2DF)
    val SecondaryText = Color(0xFF64748B)
}

private val LightColors = lightColorScheme(
    primary = MEColors.Blue,
    secondary = MEColors.Sky,
    tertiary = MEColors.Amber,
    background = MEColors.Background,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = MEColors.Navy,
    onSurface = MEColors.Navy,
    error = MEColors.Red
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF08101F),
    surface = Color(0xFF111C30),
    onPrimary = MEColors.Navy,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun MEAppsTheme(darkTheme: Boolean = isSystemInDarkTheme(), fontScaleMultiplier: Float = 1f, content: @Composable () -> Unit) {
    // Typography intentionally uses Compose defaults: no bundled font overrides the device font.
    val systemDensity = LocalDensity.current
    val adjustedDensity = Density(systemDensity.density, systemDensity.fontScale * fontScaleMultiplier.coerceIn(0.80f, 1.40f))
    CompositionLocalProvider(LocalDensity provides adjustedDensity) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}
