package com.app.wheelie_assistant

import BTParam.*
import JsonParamParser
import android.content.Context
import android.util.Log
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
  private val VERSION_URL =
    "https://raw.githubusercontent.com/SamarkinAndrey/WheelieAssistantBinary/master/flash_download_tool/firmware/version.info"

  private val TAG = "OTAManager"

  private var otaCallback: IOtaCallback? = null
  private var wifiCallback: IWifiCallback? = null
  private var isConnecting = false
  private var inProgress = false
  private var isStarted = false
  private var isGetVersion = false

  interface IWifiCallback {
    fun onConnecting()
    fun onConnected()
    fun onError()
  }

  interface IOtaCallback {
    fun onStarted()
    fun onSuccess()
    fun onProgress(progress: Int)
    fun onFailed(message: String)
    fun onNotify(message: String)
  }

  fun getRemoteVersion(versionUrl: String? = null, callback: (String?) -> Unit) {
    isGetVersion = true
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
          isGetVersion = false
          callback(result)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Exception during HTTP request: ${e.message}", e)
        withContext(Dispatchers.Main) {
          isGetVersion = false
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
    wifi_callback: IWifiCallback,
    ota_callback: IOtaCallback
  ) {
    wifiCallback = wifi_callback
    otaCallback = ota_callback

    val request = JsonParamParser()
    request.setInt(B_OTA_START, 1)
    request.setString(B_WIFI_SSID, ssid)
    request.setString(B_WIFI_PASS, pass)

    bleManager.send(request)

    isStarted = true
  }

  fun processUpdate(parser: JsonParamParser) {
    if (!isStarted)
      return

    try {
      when {
        parser.getInt(B_WIFI_CONNECTING) == 1 -> {
          isConnecting = true
          wifiCallback?.onConnecting();
        }

        parser.getInt(B_WIFI_SUCCESS) == 1 -> {
          isConnecting = false
          wifiCallback?.onConnected()
        }

        parser.getInt(B_WIFI_ERROR) == 1 -> {
          isConnecting = false
          wifiCallback?.onError();
          close()
        }

        parser.getInt(B_OTA_START) == 1 -> {
          inProgress = true
          otaCallback?.onStarted()
        }

        parser.hasParam(B_OTA_SUCCESS) -> {
          otaCallback?.onSuccess()
          close()
        }

        parser.hasParam(B_OTA_PROGRESS) -> {
          otaCallback?.onProgress(parser.getInt(B_OTA_PROGRESS))
        }

        parser.hasParam(B_OTA_ERROR) -> {
          otaCallback?.onFailed(
            "Update error: ${
              parser.getString(
                B_OTA_MESSAGE,
                "Неизвестная ошибка"
              )
            }"
          )
          close()
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Ошибка обработки ответа", e)
      otaCallback?.onFailed("Ошибка: ${e.message}")
      close()
    }
  }

  private fun close() {
    isStarted = false
    inProgress = false
    isConnecting = false
    isGetVersion = false

    otaCallback = null
    wifiCallback = null
  }

  fun isStarted() = isStarted
  fun inProgress() = inProgress
  fun isConnecting(): Boolean = isConnecting
  fun isGetVersion(): Boolean = isGetVersion
}