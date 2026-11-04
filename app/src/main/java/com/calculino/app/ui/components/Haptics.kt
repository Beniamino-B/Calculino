package com.calculino.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

val LocalHapticsEnabled = staticCompositionLocalOf { true }

@Composable
fun rememberHaptics(): (HapticFeedbackType) -> Unit {
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(haptic, enabled) {
        { type: HapticFeedbackType ->
            if (enabled) haptic.performHapticFeedback(type)
        }
    }
}
