package com.example.deepworkai.network

import android.content.Context
import android.content.SharedPreferences
import com.example.deepworkai.BuildConfig

object NetworkPreferences {
    private const val PREFS_NAME = "deepwork_network_prefs"
    private const val KEY_BACKEND_URL = "backend_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        // App start hone par MainActivity se call hota hai.
        // Yeh SharedPreferences ko initialize karta hai taaki hum user ka data device me save kar sake.
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var backendUrl: String
        get() = BuildConfig.BACKEND_URL
        set(value) {
            // No-op, always use BuildConfig
        }

    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) = prefs.edit().putString("user_id", value).apply()

    var userName: String?
        get() = prefs.getString("user_name", null)
        set(value) = prefs.edit().putString("user_name", value).apply()

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit().putString("auth_token", value).apply()
}
