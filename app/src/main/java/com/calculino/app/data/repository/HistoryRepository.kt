package com.calculino.app.data.repository

import com.calculino.app.data.local.HistoryDao
import com.calculino.app.data.local.HistoryEntity
import com.calculino.app.domain.math.AngleMode
import com.calculino.app.domain.model.HistoryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepository(
    private val dao: HistoryDao,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun observeHistory(): Flow<List<HistoryEntry>> = dao.observeAll()
        .map { entries -> entries.map(HistoryEntity::toDomain) }
        .flowOn(ioDispatcher)

    suspend fun add(
        expression: String,
        result: String,
        approximation: String?,
        isExact: Boolean,
        angleMode: AngleMode,
        createdAt: Long,
    ) {
        withContext(ioDispatcher) {
            dao.insert(
                HistoryEntity(
                    expression = expression,
                    result = result,
                    approximation = approximation,
                    isExact = isExact,
                    angleMode = angleMode.name,
                    createdAt = createdAt,
                ),
            )
        }
    }

    suspend fun delete(id: Long) {
        withContext(ioDispatcher) { dao.deleteById(id) }
    }

    suspend fun clear() {
        withContext(ioDispatcher) { dao.clear() }
    }
}

private fun HistoryEntity.toDomain(): HistoryEntry = HistoryEntry(
    id = id,
    expression = expression,
    result = result,
    approximation = approximation,
    isExact = isExact,
    angleMode = AngleMode.entries.firstOrNull { it.name == angleMode } ?: AngleMode.Radians,
    createdAt = createdAt,
)
