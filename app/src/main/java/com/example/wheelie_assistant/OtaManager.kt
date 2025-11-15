package com.example.wheelie_assistant

import BTParam.*
import JsonParamParser
import android.content.Context
import android.util.Log
import com.app.wheelie_assistant.BleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class OtaManager(
  private val context: Context,
  private val bleManager: BleManager
) {
  private val TAG = "OTAManager"
  private val VERSION_URL = "https://raw.githubusercontent.com/SamarkinAndrey/WheelieAssistantBinary/master/flash_download_tool/firmware/version.info"
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

  fun getRemoteVersion(versionUrl: String? = null, callback: (String?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val trustManager = object : X509TrustManager {
          override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
          override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
          override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), SecureRandom())

        val client = OkHttpClient.Builder()
          .sslSocketFactory(sslContext.socketFactory, trustManager)
          .hostnameVerifier { _, _ -> true }
          .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
          .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
          .build()

        val request = Request.Builder()
          .url(versionUrl ?: VERSION_URL)
          .build()

        val response = client.newCall(request).execute()
        val result = if (response.isSuccessful) {
          response.body.string().trim()
        } else {
          null
        }

        withContext(Dispatchers.Main) {
          callback(result)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Exception during HTTP request: ${e.message}", e)
        withContext(Dispatchers.Main) {
          callback(null)
        }
      }
    }
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

  fun requestUpdate(
    ssid: String,
    pass: String,
    callback: IOTACallback
  ) {
    otaCallback = callback

    val request = JsonParamParser()
    request.setInt(B_FIRMWARE_START, 1)
    request.setString(B_WIFI_SSID, ssid)
    request.setString(B_WIFI_PASS, pass)

    bleManager.sendJsonParams(request)

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