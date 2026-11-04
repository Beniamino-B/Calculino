package com.calculino.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculino.app.data.repository.HistoryRepository
import com.calculino.app.domain.model.HistoryEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val retrySignal = MutableStateFlow(0)

    val uiState = retrySignal
        .flatMapLatest {
            historyRepository.observeHistory()
                .map { entries -> HistoryUiState(entries = entries, isLoading = false) }
                .catch { emit(HistoryUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )

    fun retry() {
        retrySignal.value += 1
    }

    fun delete(id: Long) {
        viewModelScope.launch { runCatching { historyRepository.delete(id) } }
    }

    fun clear() {
        viewModelScope.launch { runCatching { historyRepository.clear() } }
    }
}
