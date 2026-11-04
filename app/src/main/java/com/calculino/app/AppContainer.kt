package com.calculino.app

import android.content.Context
import com.calculino.app.data.ai.LocalAiEngine
import com.calculino.app.data.ai.LocalModelRepository
import com.calculino.app.data.currency.CurrencyApi
import com.calculino.app.data.currency.CurrencyRepository
import com.calculino.app.data.local.CalculinoDatabase
import com.calculino.app.data.mathjs.MathJsApi
import com.calculino.app.data.mathjs.MathJsApiRepository
import com.calculino.app.data.mathjs.MathJsCache
import com.calculino.app.data.ocr.MlKitOcrEngine
import com.calculino.app.data.prefs.SettingsRepository
import com.calculino.app.data.prefs.settingsDataStore
import com.calculino.app.data.repository.HistoryRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = CalculinoDatabase.build(appContext)

    val historyRepository = HistoryRepository(
        dao = database.historyDao(),
        ioDispatcher = Dispatchers.IO,
    )

    val settingsRepository = SettingsRepository(appContext.settingsDataStore)

    val currencyRepository by lazy {
        val api = Retrofit.Builder()
            .baseUrl("https://api.exchangerate-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApi::class.java)
        CurrencyRepository(api = api, ioDispatcher = Dispatchers.IO)
    }

    val mathJsRepository by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BuildConfig.MATH_JS_BASE_URL)
            .client(client)
            .build()
            .create(MathJsApi::class.java)
        MathJsApiRepository(
            api = api,
            cache = MathJsCache(appContext),
            ioDispatcher = Dispatchers.IO,
        )
    }

    val localModelRepository by lazy { LocalModelRepository(appContext, Dispatchers.IO) }

    val localAiEngine by lazy { LocalAiEngine(localModelRepository) }

    val ocrEngine by lazy { MlKitOcrEngine(appContext) }
}

private const val NETWORK_TIMEOUT_SECONDS = 10L
