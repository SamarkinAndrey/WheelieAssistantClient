package com.app.wheelie_assistant

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.fragment.app.DialogFragment
import java.io.File
import java.io.FileOutputStream

class OtaUpdateDialog : DialogFragment() {
  private lateinit var mainActivity: MainActivity
  private var firmwareFile: File? = null

  companion object {
    private const val PICK_FIRMWARE_FILE = 1001
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    mainActivity = activity as MainActivity

    val view = layoutInflater.inflate(R.layout.dialog_ota_update, null)
    val btnSelectFile = view.findViewById<Button>(R.id.btnSelectFile)
    val btnStartUpdate = view.findViewById<Button>(R.id.btnStartUpdate)
    val btnCancel = view.findViewById<Button>(R.id.btnCancel)
    val tvFileName = view.findViewById<TextView>(R.id.tvFileName)
    val tvFileSize = view.findViewById<TextView>(R.id.tvFileSize)
    val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

    btnSelectFile.setOnClickListener {
      selectFirmwareFile()
    }

    btnStartUpdate.setOnClickListener {
      firmwareFile?.let { file ->
        progressBar.visibility = ProgressBar.VISIBLE
        btnStartUpdate.isEnabled = false
        mainActivity.startFirmwareUpdate(file)
      } ?: showError("Please select a firmware file first")
    }

    btnCancel.setOnClickListener {
      mainActivity.abortFirmwareUpdate()
      dismiss()
    }

    return AlertDialog.Builder(requireContext())
      .setTitle("Firmware Update")
      .setView(view)
      .setCancelable(false)
      .create()
  }

  private fun selectFirmwareFile() {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      addCategory(Intent.CATEGORY_OPENABLE)
    }
    startActivityForResult(Intent.createChooser(intent, "Select Firmware File"), PICK_FIRMWARE_FILE)
  }

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

          // Create temp file
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
      showError("Failed to read file: ${e.message}")
    }
  }

  private fun updateFileInfo(fileName: String, fileSize: Long) {
    val view = dialog?.findViewById<TextView>(R.id.tvFileName)
    view?.text = "File: $fileName"

    val sizeView = dialog?.findViewById<TextView>(R.id.tvFileSize)
    sizeView?.text = "Size: ${fileSize / 1024} KB"

    dialog?.findViewById<Button>(R.id.btnStartUpdate)?.isEnabled = true
  }

  private fun showError(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
  }
}