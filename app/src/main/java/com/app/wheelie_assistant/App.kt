package com.app.wheelie_assistant

import android.app.Application
import java.lang.ref.WeakReference
import com.app.wheelie_assistant.MainActivity
import com.app.wheelie_assistant.SettingsActivity

class App : Application() {

  companion object {

    private var _mainActivityRef: WeakReference<MainActivity>? = null
    private var _settingsActivityRef: WeakReference<SettingsActivity>? = null

    var mainActivity: MainActivity?
      get() = _mainActivityRef?.get()
      set(value) {
        _mainActivityRef = if (value != null) WeakReference(value) else null
      }

    var settingsActivity: SettingsActivity?
      get() = _settingsActivityRef?.get()
      set(value) {
        _settingsActivityRef = if (value != null) WeakReference(value) else null
      }
  }

  lateinit var bleManager: AppBleManager
    private set

  lateinit var scanManager: AppBleScanManager
    private set

  lateinit var otaManager: AppOtaManager
    private set

  lateinit var prefManager: AppPrefsManager
    private set

  override fun onCreate() {
    super.onCreate()

    bleManager = AppBleManager(applicationContext)
    scanManager = AppBleScanManager(applicationContext, AppBleManager.SERVICE_UUID)
    otaManager = AppOtaManager(applicationContext, bleManager)
    prefManager = AppPrefsManager(applicationContext)
  }
}
