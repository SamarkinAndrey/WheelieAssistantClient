package com.app.wheelie_assistant

import BTParam.*
import JsonParamParser
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.app.wheelie_assistant.OtaManager.OtaCallback
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import paramParser
import java.io.File
import java.util.*

class BleManager(context: Context) : BleManager(context) {

  companion object {
    val SERVICE_UUID = UUID.fromString("00002B01-0000-1000-8000-00805F9B34FB")
    val RX_CHARACTERISTIC_UUID = UUID.fromString("00002B02-0000-1000-8000-00805F9B34FB")
    val TX_CHARACTERISTIC_UUID = UUID.fromString("00002B03-0000-1000-8000-00805F9B34FB")

    private const val TAG = "BleManager"
  }

  private var dataCallback: ((JsonParamParser) -> Unit)? = null
  private var connectionCallback: ((String) -> Unit)? = null
  private var writeErrorCallback: ((String) -> Unit)? = null

  private var rxCharacteristic: BluetoothGattCharacteristic? = null
  private var txCharacteristic: BluetoothGattCharacteristic? = null

  private var isManualDisconnect = false
  private var scanCallback: ScanCallback? = null

  private var ota = OtaManager(this)
  private var parser = JsonParamParser()

  init {
    setConnectionObserver(object : ConnectionObserver {
      override fun onDeviceConnecting(device: BluetoothDevice) {
        log(Log.DEBUG, "Device connecting: ${device.address}")
        connectionCallback?.invoke("CONNECTING")
      }

      override fun onDeviceConnected(device: BluetoothDevice) {
        log(Log.DEBUG, "Device connected: ${device.address}")
        connectionCallback?.invoke("CONNECTING")
      }

      override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        log(Log.ERROR, "Failed to connect: ${device.address}, reason: $reason")
        connectionCallback?.invoke("DISCONNECTED")
      }

      override fun onDeviceReady(device: BluetoothDevice) {
        log(Log.DEBUG, "Device ready: ${device.address}")
        connectionCallback?.invoke("READY")
      }

      override fun onDeviceDisconnecting(device: BluetoothDevice) {
        log(Log.DEBUG, "Device disconnecting: ${device.address}")
      }

      override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        log(Log.DEBUG, "Device disconnected: ${device.address}, reason: $reason")
        if (isManualDisconnect) {
          connectionCallback?.invoke("DISCONNECTED")
          isManualDisconnect = false
        } else {
          connectionCallback?.invoke("CONNECTION_LOST")
        }
      }
    })
  }

  override fun log(priority: Int, message: String) {
    Log.println(priority, TAG, message)
  }

  override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
    log(Log.DEBUG, "Checking required services...")

    val service = gatt.getService(SERVICE_UUID)
    rxCharacteristic = service?.getCharacteristic(RX_CHARACTERISTIC_UUID)
    txCharacteristic = service?.getCharacteristic(TX_CHARACTERISTIC_UUID)

    val isSupported = rxCharacteristic != null && txCharacteristic != null
    log(Log.DEBUG, "Required services supported: $isSupported")

    return isSupported
  }

  override fun initialize() {
    log(Log.DEBUG, "Initializing BLE connection...")

    requestMtu(247)
      .with { device, mtu ->
        log(Log.DEBUG, "MTU set to: $mtu")
      }
      .enqueue()

    enableTxNotifications()
  }

  override fun onServicesInvalidated() {
    log(Log.DEBUG, "Services invalidated")
    rxCharacteristic = null
    txCharacteristic = null
  }

  private fun enableTxNotifications() {
    setNotificationCallback(txCharacteristic).with { device: BluetoothDevice, data ->
      if (data.value != null) {
        val value = data.getStringValue(0) ?: ""
        log(Log.DEBUG, "Received data: $value")

        if (parser.parse(value)) {
          if (ota.inProgress())
            ota.handleResponse(parser)

          dataCallback?.invoke(parser)
        }
      }
    }

    enableNotifications(txCharacteristic)
      .done {
        log(Log.DEBUG, "TX notifications enabled")
      }
      .fail { device: BluetoothDevice, status: Int ->
        log(Log.ERROR, "Failed to enable TX notifications: $status")
      }
      .enqueue()
  }

  fun writeData(data: String) {
    if (rxCharacteristic != null) {
      val dataBytes = Data.from(data)
      writeCharacteristic(
        rxCharacteristic,
        dataBytes,
        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
      )
        .done {
          log(Log.DEBUG, "Data written successfully: $data")
        }
        .fail { device: BluetoothDevice, status: Int ->
          log(Log.ERROR, "Failed to write data: $status")
          writeErrorCallback?.invoke("Failed to send data: error $status")
        }
        .enqueue()
    } else {
      log(Log.ERROR, "RX characteristic not available")
      writeErrorCallback?.invoke("RX characteristic not available")
    }
  }

  fun sendCommands(commands: String) {
    if (rxCharacteristic != null) {
      var buf: String = commands
      try {
        if (!isJson(buf)) {
          val p = paramParser()
          if (!p.parse(buf)) return
          buf = p.toJson()
        }
        val data = "$buf\n"
        writeData(data)
      } catch (e: Exception) {
        log(Log.ERROR, "Failed to write data: ${e.message}")
        writeErrorCallback?.invoke("Failed to send data: error ${e.message}")
      }
    } else {
      log(Log.ERROR, "RX characteristic not available")
      writeErrorCallback?.invoke("RX characteristic not available")
    }
  }

  private fun isJson(jsonString: String): Boolean {
    val trimmed = jsonString.trim()
    if (trimmed.isEmpty()) return false

    return try {
      when {
        trimmed.startsWith("{") && trimmed.endsWith("}") -> {
          JSONObject(trimmed)
          true
        }

        trimmed.startsWith("[") && trimmed.endsWith("]") -> {
          JSONArray(trimmed)
          true
        }

        else -> false
      }
    } catch (e: JSONException) {
      false
    }
  }

  fun startAutoConnect() {
    log(Log.DEBUG, "Starting auto-connect to find compatible devices...")

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val leScanner = bluetoothAdapter?.bluetoothLeScanner

    if (bluetoothAdapter == null) {
      log(Log.ERROR, "Bluetooth not available")
      return
    }

    if (!bluetoothAdapter.isEnabled) {
      log(Log.ERROR, "Bluetooth is disabled")
      return
    }

    if (leScanner == null) {
      log(Log.ERROR, "BLE scanner not available")
      return
    }

    stopScanning()

    scanCallback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, result: ScanResult) {
        val device = result.device
        log(Log.DEBUG, "Found device: ${device.name ?: "Unknown"} (${device.address})")

        stopScanning()
        connectToDevice(device)
      }

      override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        connectionCallback?.invoke("DISCONNECT")
        log(Log.ERROR, "Scan failed: $errorCode")
      }
    }

    try {
      val scanFilters = listOf(
        ScanFilter.Builder()
          .setServiceUuid(ParcelUuid(SERVICE_UUID))
          .build()
      )

      val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

      leScanner.startScan(scanFilters, scanSettings, scanCallback!!)
      connectionCallback?.invoke("CONNECTING")

      log(Log.DEBUG, "BLE scan started for service: $SERVICE_UUID")
    } catch (e: SecurityException) {
      log(Log.ERROR, "Bluetooth permission denied: ${e.message}")
    } catch (e: Exception) {
      log(Log.ERROR, "BLE scan error: ${e.message}")
    }
  }

  private fun connectToDevice(device: BluetoothDevice) {
    log(Log.DEBUG, "Connecting to device: ${device.name ?: "Unknown"} (${device.address})")

    connect(device)
      .useAutoConnect(true)
      .enqueue()
  }

  private fun stopScanning() {
    if (scanCallback == null)
      return

    try {
      val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
      val bluetoothAdapter = bluetoothManager.adapter
      val leScanner = bluetoothAdapter?.bluetoothLeScanner

      scanCallback?.let { callback ->
        leScanner?.stopScan(callback)
      }
      scanCallback = null
      connectionCallback?.invoke("DISCONNECTED")

      log(Log.DEBUG, "BLE scan stopped")
    } catch (e: SecurityException) {
      log(Log.ERROR, "Security exception when stopping scan: ${e.message}")
    } catch (e: IllegalStateException) {
      log(Log.ERROR, "Illegal state when stopping scan: ${e.message}")
    } catch (e: Exception) {
      log(Log.ERROR, "Error stopping scan: ${e.message}")
    }
  }

  fun disconnectDevice() {
    isManualDisconnect = true
    stopScanning()
    cancelQueue()
    disconnect().enqueue()
  }

  fun setDataCallback(callback: (JsonParamParser) -> Unit) {
    dataCallback = callback
  }

  fun setConnectionCallback(callback: (String) -> Unit) {
    connectionCallback = callback
  }

  fun setWriteErrorCallback(callback: (String) -> Unit) {
    writeErrorCallback = callback
  }

  fun startOtaUpdate(firmwareFile: File, callback: OtaCallback) {
    ota.startUpdate(firmwareFile, callback)
  }

  fun abortOtaUpdate() {
    ota.abortUpdate()
  }

  fun updateInProgress(): Boolean = ota.inProgress()
}