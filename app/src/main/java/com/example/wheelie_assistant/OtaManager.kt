package com.app.wheelie_assistant

import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import JsonParamParser
import BTParam.*

class OtaManager(private val bleManager: BleManager) {
  companion object {
    private const val TAG = "OtaManager"
    private const val PACKET_SIZE = 200 // bytes
  }

  private var inProgress = false
  private var totalSize = 0L
  private var bytesSent = 0L
  private var firmwareFile: File? = null

  interface OtaCallback {
    fun onProgress(progress: Int, bytesSent: Long, totalSize: Long)
    fun onDeviceProgress(progress: Int)
    fun onSuccess()
    fun onError(message: String)
    fun onAcknowledged(bytesReceived: Long)
    fun onStart()
    fun onFinish()
  }

  private var otaCallback: OtaCallback? = null

  fun startUpdate(firmwareFile: File, callback: OtaCallback) {
    if (inProgress) {
      callback.onError("OTA update already in progress")
      return
    }

    this.firmwareFile = firmwareFile
    this.otaCallback = callback
    this.totalSize = firmwareFile.length()
    this.bytesSent = 0L
    this.inProgress = true

    callback.onStart()

    Log.d(TAG, "Starting OTA update, file size: $totalSize bytes")

    bleManager.sendCommands("${B_FIRMWARE_START.s()}=1,${B_FIRMWARE_SIZE.s()}=$totalSize")
  }

  fun abortUpdate() {
    if (!inProgress) return

    bleManager.sendCommands("${B_FIRMWARE_ABORT.s()}=1")
    cleanup()
  }

  fun handleResponse(parser: JsonParamParser) {
    if (!inProgress) return

    if (parser.hasParam(B_FIRMWARE_ACK)) {
      val receivedBytes = parser.getLong(B_FIRMWARE_RECEIVED, 0)
      otaCallback?.onAcknowledged(receivedBytes)

      sendNextPacket()
    }

    if (parser.hasParam(B_FIRMWARE_ERROR)) {
      val errorMessage = parser.getString(B_FIRMWARE_MESSAGE, "Unknown error")
      otaCallback?.onError(errorMessage)
      cleanup()
    }

    if (parser.hasParam(B_FIRMWARE_SUCCESS)) {
      otaCallback?.onSuccess()
      cleanup()
    }

    if (parser.hasParam(B_FIRMWARE_PROGRESS)) {
      val progress = parser.getInt(B_FIRMWARE_PROGRESS)
      Log.d("OTA", "Device progress: $progress%")

      otaCallback?.onDeviceProgress(progress)
    }
  }

  private fun sendNextPacket() {
    try {
      val file = firmwareFile ?: throw IllegalStateException("Firmware file not set")
      val inputStream = FileInputStream(file)

      if (bytesSent > 0) {
        inputStream.skip(bytesSent)
      }

      val buffer = ByteArray(PACKET_SIZE)
      val bytesRead = inputStream.read(buffer)

      if (bytesRead > 0) {
        val encodedData = Base64.encodeToString(buffer, 0, bytesRead, Base64.DEFAULT)
          .replace("\n", "")

        bleManager.sendCommands("${B_FIRMWARE_DATA.s()}=$encodedData")
        bytesSent += bytesRead

        val progress = ((bytesSent * 100) / totalSize).toInt()
        otaCallback?.onProgress(progress, bytesSent, totalSize)

        Log.d(TAG, "Sent packet: $bytesSent/$totalSize ($progress%)")
      } else {
        sendFinishCommand()
        inputStream.close()
      }
    } catch (e: Exception) {
      otaCallback?.onError("Failed to send packet: ${e.message}")
      cleanup()
    }
  }

  private fun sendFinishCommand() {
    bleManager.sendCommands("${B_FIRMWARE_END.s()}=1")
  }

  private fun cleanup() {
    inProgress = false
    firmwareFile = null
    totalSize = 0L
    bytesSent = 0L

    otaCallback?.onFinish()
  }

  fun inProgress(): Boolean = inProgress

  fun calculateFileChecksum(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val inputStream = FileInputStream(file)
    val buffer = ByteArray(8192)

    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
    inputStream.close()

    return digest.digest().joinToString("") { "%02x".format(it) }
  }
}