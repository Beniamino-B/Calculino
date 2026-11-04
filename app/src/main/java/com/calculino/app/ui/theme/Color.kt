package com.calculino.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LilacBackground = Color(0xFFFFF5F8)
val LilacBackgroundEnd = Color(0xFFF8F5FF)
val LilacSurface = Color(0xFFFFFFFF)
val LilacSurfaceMuted = Color(0xFFF4ECFB)
val LilacSurfaceStrong = Color(0xFFEDE2F7)
val DeepViolet = Color(0xFF3A2A52)
val DeepVioletMuted = Color(0xFF75648C)
val TitleRose = Color(0xFFA94A6B)
val IconBlueGrey = Color(0xFF6E7E9A)
val SoftOutline = Color(0xFFDCCFE8)
val SoftRoseContainer = Color(0xFFFBD9E5)
val SoftRoseOnContainer = Color(0xFF5A1F35)

val NightBackground = Color(0xFF1A1523)
val NightSurface = Color(0xFF282032)
val NightSurfaceMuted = Color(0xFF332941)
val NightSurfaceStrong = Color(0xFF3E3150)
val NightText = Color(0xFFF5EEFA)
val NightTextMuted = Color(0xFFC6B7D4)
val NightTitle = Color(0xFFF2A8C0)
val NightIcon = Color(0xFFA8B6CE)
val NightOutline = Color(0xFF4B3A5C)
val NightPrimary = Color(0xFFE9D5FF)
val NightPrimaryContainer = Color(0xFF4A3A60)
val NightOnPrimaryContainer = Color(0xFFF0E4FA)
val NightSecondaryContainer = Color(0xFF5A2C42)
val NightOnSecondaryContainer = Color(0xFFFFD9E6)

val PastelSky = Color(0xFF9CD5FF)
val PastelMint = Color(0xFFB5EAD7)
val PastelLavender = Color(0xFFC3B1E1)
val PastelLavenderLight = Color(0xFFDCCFF7)
val PastelRose = Color(0xFFFFB7B2)
val PastelRoseDeep = Color(0xFFFF9FB0)
val PastelPeach = Color(0xFFFFDAC1)
val PastelYellow = Color(0xFFFFE9A8)

val ErrorLight = Color(0xFFB3261E)
val ErrorDark = Color(0xFFFFB4AB)

@Immutable
data class KawaiiPalette(
    val background: Color,
    val backgroundEnd: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceStrong: Color,
    val border: Color,
    val title: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val icon: Color,
    val keyNumber: Color,
    val onKeyNumber: Color,
    val keyPlus: Color,
    val onKeyPlus: Color,
    val keyMinus: Color,
    val onKeyMinus: Color,
    val keyMultiply: Color,
    val onKeyMultiply: Color,
    val keyDivide: Color,
    val onKeyDivide: Color,
    val keyPercent: Color,
    val onKeyPercent: Color,
    val keyEquals: Color,
    val onKeyEquals: Color,
    val keyClear: Color,
    val onKeyClear: Color,
    val keyDelete: Color,
    val onKeyDelete: Color,
    val keyDecimal: Color,
    val onKeyDecimal: Color,
    val keyCurrency: Color,
    val onKeyCurrency: Color,
    val keyFunction: Color,
    val onKeyFunction: Color,
    val focusRing: Color,
    val shadow: Color,
    val decoration: Color,
    val decorationAccent: Color,
)

val LightKawaiiPalette = KawaiiPalette(
    background = LilacBackground,
    backgroundEnd = LilacBackgroundEnd,
    surface = LilacSurface,
    surfaceMuted = Color(0xFFF6EEFC),
    surfaceStrong = Color(0xFFEEE2F8),
    border = Color(0x66A98BC9),
    title = TitleRose,
    textPrimary = DeepViolet,
    textSecondary = DeepVioletMuted,
    icon = IconBlueGrey,
    keyNumber = PastelSky,
    onKeyNumber = Color(0xFF16324A),
    keyPlus = PastelMint,
    onKeyPlus = Color(0xFF143D32),
    keyMinus = PastelLavender,
    onKeyMinus = Color(0xFF2D1B40),
    keyMultiply = PastelPeach,
    onKeyMultiply = Color(0xFF4A2B14),
    keyDivide = PastelRose,
    onKeyDivide = Color(0xFF4A1A22),
    keyPercent = PastelLavenderLight,
    onKeyPercent = Color(0xFF2D1B40),
    keyEquals = PastelRoseDeep,
    onKeyEquals = Color(0xFF430F1C),
    keyClear = PastelMint,
    onKeyClear = Color(0xFF143D32),
    keyDelete = PastelPeach,
    onKeyDelete = Color(0xFF4A2B14),
    keyDecimal = PastelSky,
    onKeyDecimal = Color(0xFF16324A),
    keyCurrency = PastelYellow,
    onKeyCurrency = Color(0xFF4A3A10),
    keyFunction = PastelLavenderLight,
    onKeyFunction = Color(0xFF33224A),
    focusRing = Color(0xFF8B5CF6),
    shadow = Color(0x1F3A2A52),
    decoration = PastelLavender,
    decorationAccent = PastelRose,
)

val DarkKawaiiPalette = KawaiiPalette(
    background = NightBackground,
    backgroundEnd = Color(0xFF2D1B36),
    surface = Color(0xFF2A2035),
    surfaceMuted = Color(0xFF34273F),
    surfaceStrong = Color(0xFF40314E),
    border = Color(0x80C3B1E1),
    title = PastelRose,
    textPrimary = Color(0xFFF8F2FF),
    textSecondary = Color(0xFFCBB9DE),
    icon = Color(0xFFB9C7E4),
    keyNumber = PastelSky,
    onKeyNumber = Color(0xFF16324A),
    keyPlus = PastelMint,
    onKeyPlus = Color(0xFF143D32),
    keyMinus = PastelLavender,
    onKeyMinus = Color(0xFF2D1B40),
    keyMultiply = PastelPeach,
    onKeyMultiply = Color(0xFF4A2B14),
    keyDivide = PastelRose,
    onKeyDivide = Color(0xFF4A1A22),
    keyPercent = PastelLavenderLight,
    onKeyPercent = Color(0xFF2D1B40),
    keyEquals = PastelRoseDeep,
    onKeyEquals = Color(0xFF430F1C),
    keyClear = PastelMint,
    onKeyClear = Color(0xFF143D32),
    keyDelete = PastelPeach,
    onKeyDelete = Color(0xFF4A2B14),
    keyDecimal = PastelSky,
    onKeyDecimal = Color(0xFF16324A),
    keyCurrency = PastelYellow,
    onKeyCurrency = Color(0xFF4A3A10),
    keyFunction = PastelLavenderLight,
    onKeyFunction = Color(0xFF33224A),
    focusRing = PastelLavender,
    shadow = Color(0x66000000),
    decoration = PastelLavender,
    decorationAccent = PastelRose,
)

val LocalKawaiiPalette = staticCompositionLocalOf { LightKawaiiPalette }
