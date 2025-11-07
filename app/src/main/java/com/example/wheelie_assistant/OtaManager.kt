package com.app.wheelie_assistant

import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import JsonParamParser
import BTParam.*

class OtaManager(private val bleManager: BleManager) {
  private val TAG = "OtaManager"

  private var inProgress = false
  private var totalSize = 0L
  private var chkSum: String? = null
  private var bytesReceived = 0L
  private var firmwareFile: File? = null
  private var dataSize = 0

  interface OtaCallback {
    fun onProgress(progress: Int, bytesReceived: Long, totalSize: Long)
    fun onSuccess()
    fun onFailed(errorMessage: String)
    fun onNotify(message: String)
    fun onStart()
    fun onAbort()
  }

  private var otaCallback: OtaCallback? = null

  fun startUpdate(firmwareFile: File, callback: OtaCallback) {
    if (inProgress) {
      callback.onNotify("OTA update already in progress")
      return
    }

    this.firmwareFile = firmwareFile
    this.otaCallback = callback
    this.totalSize = firmwareFile.length()
    this.chkSum = calculateFileChecksum(firmwareFile)

    Log.d(TAG, "Starting OTA update, file size: $totalSize bytes")

    bleManager.sendCommands("${B_FIRMWARE_START.s()}=1,${B_FIRMWARE_SIZE.s()}=$totalSize,${B_FIRMWARE_CHECKSUM.s()}=$chkSum")
  }

  fun abortUpdate() {
    if (!inProgress) return

    bleManager.sendCommands("${B_FIRMWARE_ABORT.s()}=1")
    otaCallback?.onAbort()
    cleanup()
  }

  fun processUpdate(parser: JsonParamParser) {
    if (!this.inProgress) {
      if (parser.getInt(B_FIRMWARE_START, 0) == 1) {
        this.dataSize = parser.getInt(B_FIRMWARE_DATA_SIZE)

        if (this.dataSize < 1) {
          Log.d(TAG, "Data size not defined!")
          this.otaCallback?.onFailed("Data size not defined!")
          return
        }

        this.bytesReceived = 0L
        this.inProgress = true

        this.otaCallback?.onStart()

        sendNextPacket()
      }
    } else {
      if (parser.hasParam(B_FIRMWARE_RECEIVED)) {
        bytesReceived = parser.getLong(B_FIRMWARE_RECEIVED)

        val progress = ((bytesReceived * 100) / totalSize).toInt()
        Log.d(TAG, "Receive progress: $bytesReceived / $totalSize ($progress%)")

        otaCallback?.onProgress(progress, bytesReceived, totalSize)

        sendNextPacket()
      }

      if (parser.hasParam(B_FIRMWARE_ERROR)) {
        val errorMessage = parser.getString(B_FIRMWARE_MESSAGE, "Unknown error")
        otaCallback?.onFailed(errorMessage)
        cleanup()
      }

      if (parser.hasParam(B_FIRMWARE_SUCCESS)) {
        otaCallback?.onSuccess()
        cleanup()
      }
    }
  }

  private fun sendNextPacket() {
    if (bytesReceived >= totalSize)
      return

    try {
      val file = firmwareFile ?: throw IllegalStateException("Firmware file not set")
      val inputStream = FileInputStream(file)

      if (bytesReceived > 0)
        inputStream.skip(bytesReceived)

      val buffer = ByteArray(dataSize)
      val bytesRead = inputStream.read(buffer)

      if (bytesRead > 0) {
        val encodedData = Base64.encodeToString(buffer, 0, bytesRead, Base64.DEFAULT)
          .replace("\n", "")

        bleManager.sendCommands("${B_FIRMWARE_DATA.s()}=$encodedData")

        val bytesSent = bytesReceived + bytesRead
        val progress = ((bytesSent * 100) / totalSize).toInt()
        Log.d(TAG, "Sent progress: $bytesSent / $totalSize ($progress%)")
      }

      inputStream.close()
    } catch (e: Exception) {
      otaCallback?.onFailed("Failed to send packet: ${e.message}")
      cleanup()
    }
  }

  private fun cleanup() {
    inProgress = false
    firmwareFile = null
    totalSize = 0L
    chkSum = null
    bytesReceived = 0L
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