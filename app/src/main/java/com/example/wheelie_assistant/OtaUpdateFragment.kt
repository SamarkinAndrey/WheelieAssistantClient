package com.app.wheelie_assistant

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.wheelie_assistant.OtaManager
import com.google.android.material.button.MaterialButton

class OtaUpdateFragment : Fragment() {
  private lateinit var btnSelectFile: MaterialButton
  private lateinit var btnStartUpdate: MaterialButton
  private lateinit var tvFileName: TextView
  private lateinit var tvFileSize: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var tvStatus: TextView

  private var bleManager: BleManager? = null
  private var otaManager: OtaManager? = null
  private var fileUri: Uri? = null
  private var fileName: String? = null
  private var fileSize: Long? = null

  private val selectFirmwareLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      fileUri = it
      readFileInfo()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    val mainActivity = MainActivity.getInstance()
    mainActivity?.let {
      bleManager = mainActivity.bleManager
      otaManager = mainActivity.otaManager
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_ota_update, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initViews(view)
    setup()
    updateUI()
  }

  private fun initViews(view: View) {
    btnSelectFile = view.findViewById(R.id.btnSelectFile)
    btnStartUpdate = view.findViewById(R.id.btnStartUpdate)
    tvFileName = view.findViewById(R.id.tvFileName)
    tvFileSize = view.findViewById(R.id.tvFileSize)
    progressBar = view.findViewById(R.id.progressBar)
    tvStatus = view.findViewById(R.id.tvStatus)
  }

  private fun setup() {
    btnSelectFile.setOnClickListener {
      selectFirmwareLauncher.launch("*/*")
    }

    btnStartUpdate.setOnClickListener {
      otaManager?.requestUpdate(otaCallback)
    }

    progressBar.min = 0;
    progressBar.max = 100;
    progressBar.visibility = ProgressBar.VISIBLE
  }

  private fun readFileInfo() {
    fileUri?.let { uri ->
      try {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
            updateFileInfo()
          }
        }
      } catch (e: Exception) {
        showToast("Ошибка чтения файла: ${e.message}")
      }
    }
  }

  private fun updateFileInfo() {
    tvFileName.text = "Файл: $fileName"
    tvFileSize.text = "Размер: ${fileSize?.let { String.format("%.1f", it / 1024f) } ?: 0} КБ"
    btnStartUpdate.isEnabled = true
    updateStatus("Файл выбран. Нажмите «Начать обновление»")
  }

  private val otaCallback = object : OtaManager.IOTACallback {
    override fun onStarted() {
      updateStatus("Прошивка...")
      progressBar.progress = 0;
      progressBar.visibility = ProgressBar.VISIBLE
      btnStartUpdate.isEnabled = false
    }

    override fun onProgress(progress: Int) {
      progressBar.setProgress(progress, true)
    }

    override fun onSuccess() {
      updateStatus("Успешно обновлено, перезагрузка...")
      updateUI()
    }

    override fun onFailed(message: String) {
      updateStatus("Ошибка прошивки: $message")
      updateUI()
    }

    override fun onNotify(message: String) {
      updateStatus(message)
    }
  }

  private fun updateStatus(text: String) {
    tvStatus.text = text
  }

  private fun updateUI() {
    progressBar.visibility = ProgressBar.INVISIBLE
    btnStartUpdate.isEnabled = fileUri != null
//    if (btnStartUpdate.isEnabled)
//      updateStatus("Ready to firmware update")
//    else
//      updateStatus("Выберите файл прошивки")
  }

  private fun showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    Log.d("OTAManager", message)
  }
}