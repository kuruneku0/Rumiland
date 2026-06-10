package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("rumiland_prefs", Context.MODE_PRIVATE)

    fun isDarkTheme(default: Boolean = true): Boolean {
        return prefs.getBoolean("key_dark_theme", default)
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("key_dark_theme", isDark).apply()
    }

    fun getUserName(default: String = "کاربر رومی‌لند"): String {
        return prefs.getString("key_user_name", default) ?: default
    }

    fun setUserName(name: String) {
        if (name.isNotBlank()) {
            prefs.edit().putString("key_user_name", name).apply()
        }
    }
}
