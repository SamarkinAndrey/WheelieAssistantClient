package com.app.wheelie_assistant

import android.app.Application
import java.lang.ref.WeakReference

class App : Application() {
  companion object {
    private var _settingsActivityRef: WeakReference<SettingsActivity>? = null

    var settingsActivity: SettingsActivity?
      get() = _settingsActivityRef?.get()
      set(value) {
        _settingsActivityRef = if (value != null) WeakReference(value) else null
      }
  }

  lateinit var bleManager: AppBleManager
  lateinit var otaManager: AppOtaManager
  lateinit var prefManager: AppPrefsManager

  override fun onCreate() {
    super.onCreate()

    bleManager = AppBleManager(applicationContext)
    otaManager = AppOtaManager(applicationContext, bleManager)
    prefManager = AppPrefsManager(applicationContext)
 }
}