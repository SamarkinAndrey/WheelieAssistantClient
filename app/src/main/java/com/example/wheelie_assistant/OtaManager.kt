package com.example.wheelie_assistant

import BTParam.*
import JsonParamParser
import android.content.Context
import android.util.Log
import com.app.wheelie_assistant.BleManager
import java.net.HttpURLConnection
import java.net.URL

class OtaManager(
  private val context: Context,
  private val bleManager: BleManager
) {
  private val TAG = "OTAManager"
  private val VERSION_URL = "https://raw.githubusercontent.com/SamarkinAndrey/WheelieAssistantBinary/master/flash_download_tool/firmware/version.info"
  private var remoteVersion: String? = null
  private var otaCallback: IOTACallback? = null
  private var isStarted = false
  private var inProgress = false

  interface IOTACallback {
    fun onStarted()
    fun onSuccess()
    fun onProgress(progress: Int)
    fun onFailed(message: String)
    fun onNotify(message: String)
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

  fun requestUpdate(callback: IOTACallback) {
    otaCallback = callback

    bleManager.sendCommands("${B_FIRMWARE_START.value}=1")

    isStarted = true
  }

  fun processUpdate(parser: JsonParamParser) {
    if (!isStarted)
      return

    try {
      when {
        parser.getInt(B_FIRMWARE_START) == 1 -> {
          inProgress = true
          otaCallback?.onStarted()
        }

        parser.hasParam(B_FIRMWARE_SUCCESS) -> {
          otaCallback?.onSuccess()
          destroy()
        }

        parser.hasParam(B_FIRMWARE_PROGRESS) -> {
          otaCallback?.onProgress(parser.getInt(B_FIRMWARE_PROGRESS))
        }

        parser.hasParam(B_FIRMWARE_ERROR) -> {
          otaCallback?.onFailed("Update error: ${parser.getString(B_FIRMWARE_MESSAGE, "Неизвестная ошибка")}")
          destroy()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Ошибка обработки ответа", e)
      otaCallback?.onFailed("Ошибка: ${e.message}")
      destroy()
    }
  }

  private fun destroy() {
    isStarted = false
    inProgress = false
    otaCallback = null
  }

  fun inProgress() = inProgress
  fun isStarted() = isStarted
}