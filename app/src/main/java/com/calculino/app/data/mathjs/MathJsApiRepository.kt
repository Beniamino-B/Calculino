package com.calculino.app.data.mathjs

import com.calculino.app.domain.mathjs.MathJsError
import com.calculino.app.domain.mathjs.MathJsOutcome
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MathJsApiRepository(
    private val api: MathJsApi,
    private val cache: MathJsCache,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun evaluate(expression: String, precision: Int): MathJsOutcome = withContext(ioDispatcher) {
        val normalized = expression.trim()
        if (normalized.isEmpty()) {
            return@withContext MathJsOutcome.Failure(MathJsError.InvalidExpression)
        }
        cache.get(normalized, precision)?.let { cached ->
            return@withContext MathJsOutcome.Success(cached, cached = true)
        }
        try {
            val response = api.evaluate(normalized, precision.coerceIn(MIN_PRECISION, MAX_PRECISION))
            if (!response.isSuccessful) {
                val detail = response.errorBody()?.string()?.trim()?.take(MAX_DETAIL_LENGTH)
                MathJsOutcome.Failure(MathJsError.Server, detail?.takeIf(String::isNotEmpty))
            } else {
                val value = response.body()?.string()?.trim().orEmpty()
                if (value.isEmpty()) {
                    MathJsOutcome.Failure(MathJsError.EmptyResponse)
                } else {
                    cache.put(normalized, precision, value)
                    MathJsOutcome.Success(value, cached = false)
                }
            }
        } catch (_: SocketTimeoutException) {
            MathJsOutcome.Failure(MathJsError.Timeout)
        } catch (_: UnknownHostException) {
            MathJsOutcome.Failure(MathJsError.Offline)
        } catch (_: ConnectException) {
            MathJsOutcome.Failure(MathJsError.Offline)
        } catch (_: IOException) {
            MathJsOutcome.Failure(MathJsError.Network)
        }
    }

    private companion object {
        const val MIN_PRECISION = 1
        const val MAX_PRECISION = 14
        const val MAX_DETAIL_LENGTH = 180
    }
}
