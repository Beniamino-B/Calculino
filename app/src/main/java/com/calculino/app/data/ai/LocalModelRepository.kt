package com.calculino.app.data.ai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import com.calculino.app.R
import com.calculino.app.domain.ai.DownloadStartResult
import com.calculino.app.domain.ai.LocalModel
import com.calculino.app.domain.ai.LocalModelState
import com.calculino.app.domain.ai.ModelFailure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

private const val PREFS_NAME = "calculino_models"
private const val MIN_FREE_BYTES = 128L * 1024L * 1024L

class LocalModelRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(DownloadManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val models = LocalModel.entries

    fun fileFor(model: LocalModel): File {
        val directory = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?.resolve("models")
            ?: File(appContext.filesDir, "models")
        return File(directory, model.fileName)
    }

    fun observeStates(): Flow<Map<String, LocalModelState>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(loadStates())
            delay(700)
        }
    }.flowOn(ioDispatcher)

    suspend fun loadStates(): Map<String, LocalModelState> = withContext(ioDispatcher) {
        models.associate { model -> model.id to resolveState(model) }
    }

    suspend fun startDownload(model: LocalModel): DownloadStartResult = withContext(ioDispatcher) {
        val manager = downloadManager ?: return@withContext DownloadStartResult.Unavailable
        val file = fileFor(model)
        val directory = file.parentFile ?: return@withContext DownloadStartResult.Unavailable
        if (!directory.exists() && !directory.mkdirs()) {
            return@withContext DownloadStartResult.Unavailable
        }
        val available = runCatching { StatFs(directory.absolutePath).availableBytes }.getOrNull()
            ?: return@withContext DownloadStartResult.Unavailable
        if (available < model.sizeBytes + MIN_FREE_BYTES) {
            return@withContext DownloadStartResult.InsufficientSpace
        }
        runCatching {
            val previousId = preferences.getLong(downloadKey(model), -1L)
            if (previousId != -1L) manager.remove(previousId)
            if (file.exists()) file.delete()
            val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
                .setTitle(appContext.getString(R.string.ai_download_title))
                .setDescription(appContext.getString(R.string.ai_download_description))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(
                    appContext,
                    Environment.DIRECTORY_DOWNLOADS,
                    "models/${model.fileName}",
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setMimeType("application/octet-stream")
            val id = manager.enqueue(request)
            preferences.edit()
                .putLong(downloadKey(model), id)
                .remove(verifiedKey(model))
                .apply()
            DownloadStartResult.Started
        }.getOrDefault(DownloadStartResult.Unavailable)
    }

    suspend fun cancelDownload(model: LocalModel) = withContext(ioDispatcher) {
        val manager = downloadManager ?: return@withContext
        val id = preferences.getLong(downloadKey(model), -1L)
        if (id != -1L) manager.remove(id)
        preferences.edit()
            .remove(downloadKey(model))
            .remove(verifiedKey(model))
            .apply()
        fileFor(model).delete()
    }

    suspend fun deleteModel(model: LocalModel) {
        cancelDownload(model)
    }

    suspend fun ensureVerified(model: LocalModel): Boolean = withContext(ioDispatcher) {
        val file = fileFor(model)
        if (!file.exists() || file.length() != model.sizeBytes) return@withContext false
        if (preferences.getString(verifiedKey(model), null) == model.sha256) return@withContext true
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        val hash = digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
        if (hash == model.sha256) {
            preferences.edit().putString(verifiedKey(model), hash).apply()
            true
        } else {
            file.delete()
            preferences.edit()
                .remove(downloadKey(model))
                .remove(verifiedKey(model))
                .apply()
            false
        }
    }

    private fun resolveState(model: LocalModel): LocalModelState {
        val file = fileFor(model)
        val id = preferences.getLong(downloadKey(model), -1L)
        if (id != -1L) {
            val snapshot = queryDownload(id)
            if (snapshot == null) {
                preferences.edit().remove(downloadKey(model)).apply()
            } else {
                when (snapshot.status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        if (!file.exists()) {
                            preferences.edit().remove(downloadKey(model)).apply()
                        } else {
                            return if (file.length() == model.sizeBytes) {
                                LocalModelState.Ready
                            } else {
                                LocalModelState.Failed(ModelFailure.CorruptFile)
                            }
                        }
                    }
                    DownloadManager.STATUS_FAILED -> return LocalModelState.Failed(ModelFailure.DownloadFailed)
                    DownloadManager.STATUS_PAUSED,
                    DownloadManager.STATUS_PENDING,
                    DownloadManager.STATUS_RUNNING,
                    -> return LocalModelState.Downloading(
                        downloadedBytes = snapshot.downloadedBytes,
                        totalBytes = snapshot.totalBytes.takeIf { it > 0 } ?: model.sizeBytes,
                    )
                }
            }
        }
        return if (file.exists()) {
            if (file.length() == model.sizeBytes) {
                LocalModelState.Ready
            } else {
                LocalModelState.Failed(ModelFailure.CorruptFile)
            }
        } else {
            LocalModelState.NotDownloaded
        }
    }

    private fun queryDownload(id: Long): DownloadSnapshot? {
        val manager = downloadManager ?: return null
        val cursor = manager.query(DownloadManager.Query().setFilterById(id)) ?: return null
        return cursor.use {
            if (!it.moveToFirst()) return null
            DownloadSnapshot(
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)),
                downloadedBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                ),
                totalBytes = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES),
                ),
            )
        }
    }

    private fun downloadKey(model: LocalModel): String = "download_id_${model.id}"

    private fun verifiedKey(model: LocalModel): String = "verified_sha_${model.id}"

    private data class DownloadSnapshot(
        val status: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
    )

    private companion object {
        const val BUFFER_SIZE = 1024 * 1024
    }
}
