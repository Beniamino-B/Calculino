package com.calculino.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calculino.app.domain.settings.AppSettings
import com.calculino.app.domain.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "calculino_settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[THEME_MODE]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.System,
                hapticsEnabled = preferences[HAPTICS_ENABLED] ?: true,
                localAiEnabled = preferences[LOCAL_AI_ENABLED] ?: true,
                localAiModelId = preferences[LOCAL_AI_MODEL_ID] ?: "qwen2_5_1_5b",
                cameraOcrEnabled = preferences[CAMERA_OCR_ENABLED] ?: false,
                onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = mode.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setLocalAiEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[LOCAL_AI_ENABLED] = enabled }
    }

    suspend fun setLocalAiModelId(modelId: String) {
        dataStore.edit { preferences -> preferences[LOCAL_AI_MODEL_ID] = modelId }
    }

    suspend fun setCameraOcrEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[CAMERA_OCR_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences -> preferences[ONBOARDING_COMPLETED] = completed }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val LOCAL_AI_ENABLED = booleanPreferencesKey("local_ai_enabled")
        val LOCAL_AI_MODEL_ID = stringPreferencesKey("local_ai_model_id")
        val CAMERA_OCR_ENABLED = booleanPreferencesKey("camera_ocr_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
