package com.app.wheelie_assistant

import android.app.Application
import android.bluetooth.BluetoothDevice
import java.lang.ref.WeakReference

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
    otaManager = AppOtaManager(applicationContext, bleManager)
    prefManager = AppPrefsManager(applicationContext)
    scanManager = AppBleScanManager(
      applicationContext,
      AppBleManager.SERVICE_UUID,
      object : AppBleScanManager.ICallback {
        override fun onDeviceFound(device: BluetoothDevice, rssi: Int) {
          mainActivity?.onDeviceFound(device, rssi)
        }
        override fun onScanStarted() {
          mainActivity?.onScanStarted()
        }
        override fun onScanFailed(errorCode: Int) {
          mainActivity?.onScanFailed(errorCode)
        }
        override fun onScanStopped() {
          mainActivity?.onScanStopped()
        }
      }
    )
  }
}
