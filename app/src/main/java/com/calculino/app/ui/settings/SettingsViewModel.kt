package com.calculino.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.prefs.SettingsRepository
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.settings.AppSettings
import com.calculino.app.domain.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { runCatching { settingsRepository.setThemeMode(mode) } }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { runCatching { settingsRepository.setHapticsEnabled(enabled) } }
    }

    fun setLocalAiEnabled(enabled: Boolean) {
        viewModelScope.launch { runCatching { settingsRepository.setLocalAiEnabled(enabled) } }
    }

    fun setCameraOcrEnabled(enabled: Boolean) {
        viewModelScope.launch { runCatching { settingsRepository.setCameraOcrEnabled(enabled) } }
    }

    fun clearHistory() {
        viewModelScope.launch { runCatching { historyRepository.clear() } }
    }
}
