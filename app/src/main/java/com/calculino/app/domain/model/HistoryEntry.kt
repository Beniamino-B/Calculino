package com.calculino.app.domain.model

import com.calculino.app.domain.math.AngleMode

data class HistoryEntry(
    val id: Long,
    val expression: String,
    val result: String,
    val approximation: String?,
    val isExact: Boolean,
    val angleMode: AngleMode,
    val createdAt: Long,
)
