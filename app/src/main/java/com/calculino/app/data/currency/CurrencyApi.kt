package com.calculino.app.data.currency

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    @GET("v4/latest/{base}")
    suspend fun latestRates(@Path("base") base: String): CurrencyRatesResponse
}

@Keep
data class CurrencyRatesResponse(
    @SerializedName("base") val base: String? = null,
    @SerializedName("rates") val rates: Map<String, Double>? = null,
)
