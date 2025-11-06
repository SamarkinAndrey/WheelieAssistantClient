package com.app.wheelie_assistant

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import com.google.android.material.button.MaterialButton

class OtaUpdateFragment : Fragment() {
  private lateinit var btnSelectFile: MaterialButton
  private lateinit var btnStartUpdate: MaterialButton
  private lateinit var btnCancel: MaterialButton
  private lateinit var tvFileName: TextView
  private lateinit var tvFileSize: TextView
  private lateinit var progressBar: ProgressBar
  private var mainActivity: MainActivity? = null
  private var firmwareFile: File? = null

  companion object {
    private const val PICK_FIRMWARE_FILE = 1001
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_ota_update, container, false)

    mainActivity = MainActivity.getInstance()

    initViews(view)
    setup()

    return view
  }

  private fun initViews(view: View) {
    btnSelectFile = view.findViewById(R.id.btnSelectFile)
    btnStartUpdate = view.findViewById(R.id.btnStartUpdate)
    btnCancel = view.findViewById(R.id.btnCancel)
    tvFileName = view.findViewById(R.id.tvFileName)
    tvFileSize = view.findViewById(R.id.tvFileSize)
    progressBar = view.findViewById(R.id.progressBar)
  }

  private fun setup() {
    btnSelectFile.setOnClickListener {
      selectFirmwareFile()
    }

    btnStartUpdate.setOnClickListener {
      firmwareFile?.let { file ->
        progressBar.visibility = ProgressBar.VISIBLE
        btnStartUpdate.isEnabled = false
        startFirmwareUpdate(file)
      } ?: showToast("Please select a firmware file first")
    }

    btnCancel.setOnClickListener {
      abortFirmwareUpdate()
    }
  }

  private fun selectFirmwareFile() {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      addCategory(Intent.CATEGORY_OPENABLE)
    }
    startActivityForResult(Intent.createChooser(intent, "Select Firmware File"), PICK_FIRMWARE_FILE)
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == PICK_FIRMWARE_FILE && resultCode == android.app.Activity.RESULT_OK) {
      data?.data?.let { uri ->
        copyFileFromUri(uri)
      }
    }
  }

  private fun copyFileFromUri(uri: Uri) {
    try {
      requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val displayName = cursor.getString(
            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
          )
          val size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))

          val tempFile = File(requireContext().cacheDir, displayName)
          requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
              inputStream.copyTo(outputStream)
            }
          }

          firmwareFile = tempFile
          updateFileInfo(displayName, size)
        }
      }
    } catch (e: Exception) {
      showToast("Failed to read file: ${e.message}")
    }
  }

  private fun updateFileInfo(fileName: String, fileSize: Long) {
    tvFileName.text = "Файл: $fileName"
    tvFileSize.text = "Размер: ${fileSize / 1024} KB"

    btnStartUpdate.isEnabled = true
  }

  private fun showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
  }

  fun startFirmwareUpdate(firmwareFile: File) {
    mainActivity?.bleManager?.startOtaUpdate(firmwareFile, object : OtaManager.OtaCallback {
      override fun onProgress(progress: Int, bytesSent: Long, totalSize: Long) {
        if (!btnCancel.isEnabled)
          btnCancel.isEnabled = true

        showToast("OTA Progress: $progress% ($bytesSent/$totalSize)")
      }

      override fun onDeviceProgress(progress: Int) {
        if (!btnCancel.isEnabled)
          btnCancel.isEnabled = true

        showToast("Device Progress: $progress%")
      }

      override fun onSuccess() {
        if (!btnCancel.isEnabled)
          btnCancel.isEnabled = true

        showToast("OTA update completed successfully! Device will restart.")
      }

      override fun onError(message: String) {
        showToast("OTA Error: $message")
      }

      override fun onAcknowledged(bytesReceived: Long) {
        Log.d("OTA", "Device acknowledged: $bytesReceived bytes")
      }

      override fun onStart() {
        btnCancel.isEnabled = true
      }

      override fun onFinish() {
        btnCancel.isEnabled = false
      }
    })
  }

  fun abortFirmwareUpdate() {
    mainActivity?.let{
      if (!it.bleManager.updateInProgress())
        return

      it.bleManager.abortOtaUpdate()
      showToast("OTA update aborted")
    }
  }
}
