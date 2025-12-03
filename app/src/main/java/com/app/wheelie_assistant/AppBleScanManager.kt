package com.app.wheelie_assistant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import no.nordicsemi.android.support.v18.scanner.*
import java.util.UUID

class AppBleScanManager(
  private val context: Context,
  private val serviceUuid: UUID
) {
  interface Callback {
    fun onDeviceFound(device: BluetoothDevice, rssi: Int)
    fun onScanStarted()
    fun onScanStopped()
    fun onScanFailed(errorCode: Int)
  }

  private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
  private var callback: Callback? = null
  private var isScanning = false

  private val scanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      val scanRecord = result.scanRecord
      val serviceUuids = scanRecord?.serviceUuids

      if (serviceUuids?.any { it.uuid == serviceUuid } == true) {
        callback?.onDeviceFound(result.device, result.rssi)
      }
    }

    override fun onBatchScanResults(results: List<ScanResult>) {
      results.forEach { result ->
        onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
      }
    }

    override fun onScanFailed(errorCode: Int) {
      isScanning = false
      callback?.onScanFailed(errorCode)
      callback = null
    }
  }

  fun startScan(callback: Callback): Boolean {
    if (isScanning) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_ALREADY_STARTED)
      return false
    }

    if (!hasPermissions()) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
      return false
    }

    val adapter = bluetoothAdapter ?: return false
    if (!adapter.isEnabled) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
      return false
    }

    this.callback = callback

    val scanner = BluetoothLeScannerCompat.getScanner()

    val filter = ScanFilter.Builder()
      .setServiceUuid(ParcelUuid(serviceUuid))
      .build()

    val settings = ScanSettings.Builder()
      .setLegacy(false)
      .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
      .setReportDelay(0)
      .setUseHardwareBatchingIfSupported(false)
      .build()

    return try {
      scanner.startScan(listOf(filter), settings, scanCallback)
      isScanning = true
      callback.onScanStarted()
      true
    } catch (e: SecurityException) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
      false
    } catch (e: IllegalStateException) {
      callback.onScanFailed(ScanCallback.SCAN_FAILED_INTERNAL_ERROR)
      false
    }
  }

  fun stopScan() {
    if (!isScanning) return

    try {
      val scanner = BluetoothLeScannerCompat.getScanner()
      scanner.stopScan(scanCallback)
      isScanning = false
    } catch (e: Exception) {
      //
    } finally {
      callback?.onScanStopped()
      callback = null
    }
  }

  private fun hasPermissions(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
        PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun isScanning(): Boolean = isScanning

  fun isBluetoothEnabled(): Boolean {
    return bluetoothAdapter?.isEnabled == true
  }

  fun release() {
    stopScan()
  }
}