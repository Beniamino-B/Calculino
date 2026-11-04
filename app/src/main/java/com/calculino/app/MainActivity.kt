package com.calculino.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculino.app.domain.settings.ThemeMode
import com.calculino.app.ui.components.LocalHapticsEnabled
import com.calculino.app.ui.navigation.CalculinoApp
import com.calculino.app.ui.onboarding.OnboardingScreen
import com.calculino.app.ui.theme.CalculinoTheme
import com.calculino.app.ui.theme.LocalKawaiiPalette
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CalculinoApplication).container
        setContent {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = null,
            )
            val scope = rememberCoroutineScope()
            val darkTheme = when (settings?.themeMode ?: ThemeMode.System) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            CalculinoTheme(darkTheme = darkTheme) {
                val palette = LocalKawaiiPalette.current
                val currentSettings = settings
                when {
                    currentSettings == null -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.background),
                    )
                    !currentSettings.onboardingCompleted -> CompositionLocalProvider(
                        LocalHapticsEnabled provides currentSettings.hapticsEnabled,
                    ) {
                        OnboardingScreen(
                            initialThemeMode = currentSettings.themeMode,
                            initialLocalAiEnabled = currentSettings.localAiEnabled,
                            initialCameraOcrEnabled = currentSettings.cameraOcrEnabled,
                            onThemeModeSelected = { mode ->
                                scope.launch {
                                    container.settingsRepository.setThemeMode(mode)
                                }
                            },
                            onLocalAiSelected = { enabled ->
                                scope.launch {
                                    container.settingsRepository.setLocalAiEnabled(enabled)
                                }
                            },
                            onLocalAiModelSelected = { modelId ->
                                scope.launch {
                                    container.settingsRepository.setLocalAiModelId(modelId)
                                }
                            },
                            onCameraOcrSelected = { enabled ->
                                scope.launch {
                                    container.settingsRepository.setCameraOcrEnabled(enabled)
                                }
                            },
                            onComplete = {
                                scope.launch {
                                    container.settingsRepository.setOnboardingCompleted(true)
                                }
                            },
                        )
                    }
                    else -> CompositionLocalProvider(
                        LocalHapticsEnabled provides currentSettings.hapticsEnabled,
                    ) {
                        CalculinoApp(container = container)
                    }
                }
            }
        }
    }
}
