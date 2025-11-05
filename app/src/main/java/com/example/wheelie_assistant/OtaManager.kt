package com.app.wheelie_assistant

import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import JsonParamParser

class OtaManager(private val bleManager: BleManager) {
  companion object {
    private const val TAG = "OtaManager"
    private const val PACKET_SIZE = 200 // bytes
  }

  private var isOtaInProgress = false
  private var totalSize = 0L
  private var bytesSent = 0L
  private var firmwareFile: File? = null

  interface OtaCallback {
    fun onProgress(progress: Int, bytesSent: Long, totalSize: Long)
    fun onSuccess()
    fun onError(message: String)
    fun onAcknowledged(bytesReceived: Long)
  }

  private var otaCallback: OtaCallback? = null

  fun startOtaUpdate(firmwareFile: File, callback: OtaCallback) {
    if (isOtaInProgress) {
      callback.onError("OTA update already in progress")
      return
    }

    this.firmwareFile = firmwareFile
    this.otaCallback = callback
    this.totalSize = firmwareFile.length()
    this.bytesSent = 0L
    this.isOtaInProgress = true

    Log.d(TAG, "Starting OTA update, file size: $totalSize bytes")

    // Send start command
    val startCommand = """
            {
                "${BTParam.B_FIRMWARE_START.s()}": 1,
                "${BTParam.B_FIRMWARE_SIZE.s()}": $totalSize
            }
        """.trimIndent()

    bleManager.writeData(startCommand)
  }

  fun abortOtaUpdate() {
    if (!isOtaInProgress) return

    val abortCommand = """
            {
                "${BTParam.B_FIRMWARE_ABORT.s()}": 1
            }
        """.trimIndent()

    bleManager.writeData(abortCommand)
    cleanup()
  }

  fun handleOtaResponse(parser: JsonParamParser) {
    if (!isOtaInProgress) return

    // Handle ACK
    if (parser.hasParam(BTParam.B_FIRMWARE_ACK)) {
      val receivedBytes = parser.getLong(BTParam.B_FIRMWARE_RECEIVED, 0)
      otaCallback?.onAcknowledged(receivedBytes)

      // Continue sending data
      sendNextPacket()
    }

    // Handle error
    if (parser.hasParam(BTParam.B_FIRMWARE_ERROR)) {
      val errorMessage = parser.getString(BTParam.B_FIRMWARE_MESSAGE, "Unknown error")
      otaCallback?.onError(errorMessage)
      cleanup()
    }

    // Handle success
    if (parser.hasParam(BTParam.B_FIRMWARE_SUCCESS)) {
      otaCallback?.onSuccess()
      cleanup()
    }
  }

  private fun sendNextPacket() {
    try {
      val file = firmwareFile ?: throw IllegalStateException("Firmware file not set")
      val inputStream = FileInputStream(file)

      // Skip already sent bytes
      if (bytesSent > 0) {
        inputStream.skip(bytesSent)
      }

      val buffer = ByteArray(PACKET_SIZE)
      val bytesRead = inputStream.read(buffer)

      if (bytesRead > 0) {
        // Encode to base64 for safe transmission
        val encodedData = Base64.encodeToString(buffer, 0, bytesRead, Base64.DEFAULT)
          .replace("\n", "") // Remove newlines

        val dataCommand = """
                    {
                        "${BTParam.B_FIRMWARE_DATA.s()}": "$encodedData"
                    }
                """.trimIndent()

        bleManager.writeData(dataCommand)
        bytesSent += bytesRead

        // Calculate progress
        val progress = ((bytesSent * 100) / totalSize).toInt()
        otaCallback?.onProgress(progress, bytesSent, totalSize)

        Log.d(TAG, "Sent packet: $bytesSent/$totalSize ($progress%)")
      } else {
        // End of file - send finish command
        sendFinishCommand()
        inputStream.close()
      }
    } catch (e: Exception) {
      otaCallback?.onError("Failed to send packet: ${e.message}")
      cleanup()
    }
  }

  private fun sendFinishCommand() {
    val finishCommand = """
            {
                "${BTParam.B_FIRMWARE_END.s()}": 1
            }
        """.trimIndent()

    bleManager.writeData(finishCommand)
  }

  private fun cleanup() {
    isOtaInProgress = false
    firmwareFile = null
    totalSize = 0L
    bytesSent = 0L
  }

  fun isOtaInProgress(): Boolean = isOtaInProgress

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