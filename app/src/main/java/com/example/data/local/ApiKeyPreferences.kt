package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object ApiKeyPreferences {
    private const val PREFS_NAME = "jee_app_prefs"
    private const val KEY_GEMINI_API_KEY = "custom_gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getApiKey(context: Context): String {
        val userKey = getPrefs(context).getString(KEY_GEMINI_API_KEY, "")?.trim().orEmpty()
        if (userKey.isNotBlank()) {
            return userKey
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun saveApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun clearApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun hasApiKey(context: Context): Boolean {
        return getApiKey(context).isNotBlank()
    }

    fun getMaskedApiKey(context: Context): String {
        val key = getApiKey(context)
        if (key.isBlank()) return ""
        return if (key.length > 8) {
            "${key.take(4)}...${key.takeLast(4)}"
        } else {
            "••••••••"
        }
    }
}
