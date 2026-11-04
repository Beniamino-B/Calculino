package com.calculino.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TitleRose,
    onPrimary = LilacSurface,
    primaryContainer = PastelLavender,
    onPrimaryContainer = DeepViolet,
    secondary = PastelLavender,
    onSecondary = DeepViolet,
    secondaryContainer = SoftRoseContainer,
    onSecondaryContainer = SoftRoseOnContainer,
    tertiary = PastelMint,
    onTertiary = DeepViolet,
    background = LilacBackground,
    onBackground = DeepViolet,
    surface = LilacSurface,
    onSurface = DeepViolet,
    surfaceVariant = Color(0xFFF6EEFC),
    onSurfaceVariant = DeepVioletMuted,
    surfaceContainerLowest = LilacSurface,
    surfaceContainerLow = LilacSurface,
    surfaceContainer = Color(0xFFF6EEFC),
    surfaceContainerHigh = Color(0xFFEEE2F8),
    surfaceContainerHighest = Color(0xFFE5D7F4),
    outline = DeepVioletMuted,
    outlineVariant = Color(0x66A98BC9),
    error = ErrorLight,
    onError = LilacSurface,
    errorContainer = SoftRoseContainer,
    onErrorContainer = SoftRoseOnContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = PastelRose,
    onPrimary = Color(0xFF430F1C),
    primaryContainer = Color(0xFF4A3A60),
    onPrimaryContainer = Color(0xFFF0E4FA),
    secondary = PastelLavender,
    onSecondary = Color(0xFF2D1B40),
    secondaryContainer = Color(0xFF4B3A60),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = PastelMint,
    onTertiary = Color(0xFF143D32),
    background = NightBackground,
    onBackground = Color(0xFFF8F2FF),
    surface = Color(0xFF2A2035),
    onSurface = Color(0xFFF8F2FF),
    surfaceVariant = Color(0xFF34273F),
    onSurfaceVariant = Color(0xFFCBB9DE),
    surfaceContainerLowest = NightBackground,
    surfaceContainerLow = Color(0xFF2A2035),
    surfaceContainer = Color(0xFF34273F),
    surfaceContainerHigh = Color(0xFF3D2E4B),
    surfaceContainerHighest = Color(0xFF4A3A5C),
    outline = Color(0xFFB9A7CC),
    outlineVariant = Color(0x80C3B1E1),
    error = ErrorDark,
    onError = Color(0xFF430F1C),
    errorContainer = Color(0xFF5A2C42),
    onErrorContainer = Color(0xFFFFD9E6),
)

@Composable
fun CalculinoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val kawaiiPalette = if (darkTheme) DarkKawaiiPalette else LightKawaiiPalette
    val view = LocalView.current
    if (!LocalInspectionMode.current) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalKawaiiPalette provides kawaiiPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CalculinoTypography,
            shapes = CalculinoShapes,
            content = content,
        )
    }
}
