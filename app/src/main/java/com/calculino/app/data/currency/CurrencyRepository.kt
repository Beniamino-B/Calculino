package com.calculino.app.data.currency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CurrencyRepository(
    private val api: CurrencyApi,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun latestRates(base: String): Result<Map<String, Double>> = withContext(ioDispatcher) {
        try {
            val response = api.latestRates(base)
            val rates = response.rates?.takeIf { values -> values.isNotEmpty() }
                ?: error("Currency API returned no rates")
            Result.success(rates)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            Result.failure(failure)
        }
    }
}
