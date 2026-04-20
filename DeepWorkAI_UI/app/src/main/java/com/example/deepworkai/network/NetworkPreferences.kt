package com.example.deepworkai.network

import android.content.Context
import android.content.SharedPreferences
import com.example.deepworkai.BuildConfig

object NetworkPreferences {
    private const val PREFS_NAME = "deepwork_network_prefs"
    private const val KEY_BACKEND_URL = "backend_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var backendUrl: String
        get() = if (::prefs.isInitialized) {
            prefs.getString(KEY_BACKEND_URL, BuildConfig.BACKEND_URL) ?: BuildConfig.BACKEND_URL
        } else {
            BuildConfig.BACKEND_URL
        }
        set(value) {
            if (::prefs.isInitialized) {
                prefs.edit().putString(KEY_BACKEND_URL, value).apply()
            }
        }

    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit().putString("user_id", value).apply()

    var userName: String?
        get() = prefs.getString("user_name", null)
        set(value) = prefs.edit().putString("user_name", value).apply()
}
