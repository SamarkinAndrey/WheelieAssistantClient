package com.app.wheelie_assistant

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(private val context: Context) {

  private val sharedPrefs: SharedPreferences by lazy {
    context.getSharedPreferences("app_string_settings", Context.MODE_PRIVATE)
  }

  fun save(key: String, value: String) {
    sharedPrefs.edit().putString(key, value).apply()
  }

  fun load(key: String, defaultValue: String = ""): String {
    return sharedPrefs.getString(key, defaultValue) ?: defaultValue
  }

  fun hasKey(key: String): Boolean {
    return sharedPrefs.contains(key)
  }

  fun remove(key: String) {
    sharedPrefs.edit().remove(key).apply()
  }

  fun clear() {
    sharedPrefs.edit().clear().apply()
  }
}