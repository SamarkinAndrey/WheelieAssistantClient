package com.app.wheelie_assistant

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import paramParser
import java.util.*

class AppBleManager(context: Context) : BleManager(context) {

  companion object {
    private const val TAG = "AppBleManager"

    val SERVICE_UUID: UUID = UUID.fromString("00002B01-0000-1000-8000-00805F9B34FB")
    val RX_UUID: UUID      = UUID.fromString("00002B02-0000-1000-8000-00805F9B34FB")
    val TX_UUID: UUID      = UUID.fromString("00002B03-0000-1000-8000-00805F9B34FB")
  }

  private var rxChar: BluetoothGattCharacteristic? = null
  private var txChar: BluetoothGattCharacteristic? = null

  private val parser = JsonParamParser()

  var onRead: ((JsonParamParser) -> Unit)? = null
  var onWriteError: ((String) -> Unit)? = null
  var onState: ((String) -> Unit)? = null

  init {
    setConnectionObserver(object : ConnectionObserver {

      override fun onDeviceConnecting(device: android.bluetooth.BluetoothDevice) {
        log(Log.DEBUG, "Connecting…")
        onState?.invoke("CONNECTING")
      }

      override fun onDeviceConnected(device: android.bluetooth.BluetoothDevice) {
        log(Log.DEBUG, "Connected (GATT).")
      }

      override fun onDeviceReady(device: android.bluetooth.BluetoothDevice) {
        log(Log.DEBUG, "Device READY.")
        onState?.invoke("READY")
      }

      override fun onDeviceFailedToConnect(device: android.bluetooth.BluetoothDevice, reason: Int) {
        log(Log.ERROR, "Failed to connect: $reason")
        onState?.invoke("DISCONNECTED")
      }

      override fun onDeviceDisconnecting(device: android.bluetooth.BluetoothDevice) {
        log(Log.DEBUG, "Disconnecting…")
      }

      override fun onDeviceDisconnected(device: android.bluetooth.BluetoothDevice, reason: Int) {
        log(Log.DEBUG, "Disconnected: reason=$reason")

        if (reason == 0) {
          onState?.invoke("DISCONNECTED")
        } else {
          onState?.invoke("CONNECTION_LOST")
        }
      }
    })
  }

  override fun log(priority: Int, message: String) {
    Log.println(priority, TAG, message)
  }

  override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
    val service = gatt.getService(SERVICE_UUID)

    rxChar = service?.getCharacteristic(RX_UUID)
    txChar = service?.getCharacteristic(TX_UUID)

    val ok = rxChar != null && txChar != null
    log(Log.DEBUG, "Service supported: $ok")
    return ok
  }

  override fun initialize() {
    requestMtu(247).enqueue()

    setNotificationCallback(txChar).with { _, data ->
      val text = data.getStringValue(0)
      if (text != null) {
        log(Log.DEBUG, "RX: $text")
        if (parser.parse(text))
          onRead?.invoke(parser)
      }
    }

    enableNotifications(txChar)
      .done { log(Log.DEBUG, "Notifications ENABLED") }
      .fail { _, status ->
        log(Log.ERROR, "Failed to enable notifications: $status")
      }
      .enqueue()
  }

  override fun onServicesInvalidated() {
    rxChar = null
    txChar = null
  }

  fun send(parser: JsonParamParser) {
    if (parser.isEmpty())
      return;

    send(parser.serialize());
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

  fun send(jsonString: String) {
    val ch = rxChar ?: run {
      onWriteError?.invoke("RX not ready")
      return
    }

    var buf: String = jsonString
    try {
      if (!isJson(buf)) {
        val p = paramParser()
        if (!p.parse(buf)) return
        buf = p.toJson()
      }

      val data = Data.from("$buf\n")

      writeCharacteristic(ch, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        .done { log(Log.DEBUG, "TX OK: $jsonString") }
        .fail { _, status ->
          log(Log.ERROR, "Write failed: $status")
          onWriteError?.invoke("Write failed: $status")
        }
        .enqueue()
    } catch (e: Exception) {
      log(Log.ERROR, "Write failed: ${e.message}")
      onWriteError?.invoke("Write failed: ${e.message}")
    }
  }
}