package com.calculino.app.domain.settings

enum class ThemeMode {
    System,
    Light,
    Dark,
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val hapticsEnabled: Boolean = true,
    val localAiEnabled: Boolean = true,
    val localAiModelId: String = "qwen2_5_1_5b",
    val cameraOcrEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
)
