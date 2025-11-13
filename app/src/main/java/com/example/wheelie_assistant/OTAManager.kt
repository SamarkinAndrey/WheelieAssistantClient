package com.example.wheelie_assistant

import BTParam.*
import JsonParamParser
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.app.wheelie_assistant.BleManager
import okhttp3.*
import java.net.HttpURLConnection
import java.net.URL

class OTAManager(
  private val context: Context,
  private val bleManager: BleManager,
  private var callback: IOTACallback
) {
  private val TAG = "OTAManager"
  private val VERSION_URL = "https://raw.githubusercontent.com/SamarkinAndrey/WheelieAssistantBinary/master/flash_download_tool/firmware/version.info"
  private val handler = Handler(Looper.getMainLooper())
  private var inProgress = false
  private var otaSsid: String = ""
  private var otaPass: String = ""
  private var otaUrl: String = ""
  private var firmwareUri: Uri? = null
  private var remoteVersion: String? = null

  interface IOTACallback {
    fun onStarted()
    fun onAborted()
    fun onSuccess()
    fun onProgress(progress: Int)
    fun onFailed(message: String)
    fun onNotify(message: String)
  }

  fun startUpdate(uri: Uri) {
    if (inProgress) {
      callback.onNotify("Обновление уже идёт")
      return
    }
    inProgress = true
    firmwareUri = uri

    callback.onStarted()

    bleManager.sendCommands("${B_FIRMWARE_START.s()}=1")
    Log.d(TAG, "Команда FIRMWARE_START отправлена")
  }

  fun compareVersions(version1: String, version2: String): Int {
    val components1 = version1.splitToVersionComponents()
    val components2 = version2.splitToVersionComponents()

    return components1.zip(components2) { a, b ->
      a.compareTo(b)
    }.firstOrNull { it != 0 } ?: components1.size.compareTo(components2.size)
  }

  private fun String.splitToVersionComponents(): List<Int> {
    return split('.').map { it.toIntOrNull() ?: 0 }
  }

  fun checkRemoteVersion(versionUrl: String? = null): Boolean {
    return runCatching {
      val url = URL(versionUrl?: VERSION_URL)
      (url.openConnection() as HttpURLConnection).run {
        requestMethod = "GET"
        connectTimeout = 10000
        readTimeout = 10000

        if (responseCode == HttpURLConnection.HTTP_OK) {
          inputStream.bufferedReader().use { reader ->
            remoteVersion = reader.readText().trim()
          }
          true
        } else {
          println("HTTP GET failed, error: $responseCode")
          false
        }
      }
    }.onFailure { error ->
      println("HTTP GET failed, error: ${error.message}")
    }.getOrDefault(false)
  }

  fun processUpdate(parser: JsonParamParser) {
    if (!inProgress) return

    try {
      when {
        parser.getInt(B_FIRMWARE_START) == 1 -> {
//          otaSsid = parser.getString(B_FIRMWARE_SSID, "")
//          otaPass = parser.getString(B_FIRMWARE_PASS, "")
//          otaUrl = parser.getString(B_FIRMWARE_URL, "")
//          if (otaSsid.isEmpty() || otaPass.isEmpty() || otaUrl.isEmpty()) {
//            callback.onFailed("ESP32 не прислал данные")
//            destroy()
//            return
//          }
//          Log.d(TAG, "Получены SSID: $otaSsid, PASS: $otaPass, URL: $otaUrl")
//          callback.onNotify("Подключение к точке доступа ESP32...")

          callback.onStarted()

//          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            connectToWifiAP()
//          } else {
//            callback.onFailed("Требуется Android 10+ для подключения к Wi-Fi")
//            destroy()
//          }
        }

        parser.hasParam(B_FIRMWARE_PROGRESS) -> {
          val p = parser.getInt(B_FIRMWARE_PROGRESS)
          callback.onProgress(p)
        }

        parser.hasParam(B_FIRMWARE_SUCCESS) -> {
          callback.onSuccess()
          destroy()
        }

        parser.hasParam(B_FIRMWARE_ERROR) -> {
          val msg = parser.getString(B_FIRMWARE_MESSAGE, "Неизвестная ошибка")
          callback.onFailed("Ошибка прошивки: $msg")
          destroy()
        }

        parser.hasParam(B_FIRMWARE_ABORT) -> {
          callback.onAborted()
          destroy()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Ошибка обработки ответа", e)
      callback.onFailed("Ошибка: ${e.message}")
      destroy()
    }
  }

  fun abort() {
    if (!inProgress) return

    bleManager.sendCommands("${B_FIRMWARE_ABORT.s()}=1")
    callback.onAborted()
    destroy()
  }

  private fun destroy() {
    if (inProgress) {
      inProgress = false
      firmwareUri = null
    }
    handler.removeCallbacksAndMessages(null)
  }

  fun inProgress() = inProgress
}