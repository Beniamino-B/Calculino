package com.calculino.app.data.mathjs

import android.content.Context
import java.security.MessageDigest

class MathJsCache(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun get(expression: String, precision: Int): String? {
        val key = cacheKey(expression, precision)
        return preferences.getString(key, null)
    }

    fun put(expression: String, precision: Int, value: String) {
        val key = cacheKey(expression, precision)
        val keys = preferences.getStringSet(INDEX_KEY, emptySet()).orEmpty().toMutableSet()
        if (key !in keys && keys.size >= MAX_ENTRIES) {
            val oldest = keys.firstOrNull()
            if (oldest != null) {
                keys.remove(oldest)
                preferences.edit().remove(oldest).apply()
            }
        }
        keys += key
        preferences.edit()
            .putString(key, value)
            .putStringSet(INDEX_KEY, keys)
            .apply()
    }

    private fun cacheKey(expression: String, precision: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$precision|$expression".toByteArray())
            .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return "result_$digest"
    }

    private companion object {
        const val PREFERENCES_NAME = "calculino_mathjs_cache"
        const val INDEX_KEY = "cache_index"
        const val MAX_ENTRIES = 100
    }
}
