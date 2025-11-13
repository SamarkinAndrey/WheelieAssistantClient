package com.app.wheelie_assistant

import BTParam.*
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
import com.example.wheelie_assistant.OTAManager
import com.google.android.material.button.MaterialButton

class OtaUpdateFragment : Fragment() {
  private lateinit var btnSelectFile: MaterialButton
  private lateinit var btnStartUpdate: MaterialButton
  private lateinit var btnCancel: MaterialButton
  private lateinit var tvFileName: TextView
  private lateinit var tvFileSize: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var tvStatus: TextView

  private var bleManager: BleManager? = null
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
    btnCancel = view.findViewById(R.id.btnCancel)
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
      startFirmwareUpdate()
    }

    btnCancel.setOnClickListener {
      abortFirmwareUpdate()
    }
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

  private fun startFirmwareUpdate() {
    if (fileUri == null) {
      showToast("Сначала выберите файл прошивки")
      return
    }

    bleManager?.sendCommands("${B_FIRMWARE_START.value}=1")
//    bleManager?.startOtaUpdate(fileUri!!, otaCallback)
  }

  private val otaCallback = object : OTAManager.IOTACallback {
    override fun onStarted() {
      updateStatus("Отправка команды на устройство...")
      progressBar.visibility = ProgressBar.VISIBLE
      btnStartUpdate.isEnabled = false
      btnCancel.isEnabled = true
    }

    override fun onProgress(progress: Int) {
      progressBar.progress = progress
      updateStatus("Обновление: $progress%")
    }

    override fun onSuccess() {
      showToast("Обновление успешно завершено!")
      updateStatus("Готово! Устройство перезагружается...")
      resetUI()
    }

    override fun onFailed(message: String) {
      showToast("Ошибка: $message")
      updateStatus("Ошибка: $message")
      resetUI()
    }

    override fun onNotify(message: String) {
      updateStatus(message)
    }

    override fun onAborted() {
      showToast("Обновление отменено")
      updateStatus("Отменено пользователем")
      resetUI()
    }
  }

  private fun abortFirmwareUpdate() {
    bleManager?.abortOtaUpdate()
  }

  private fun updateStatus(text: String) {
    tvStatus.text = text
  }

  private fun resetUI() {
    progressBar.visibility = ProgressBar.INVISIBLE
    btnStartUpdate.isEnabled = fileUri != null
    btnCancel.isEnabled = false
    updateStatus("Готов к обновлению")
  }

  private fun updateUI() {
    btnStartUpdate.isEnabled = fileUri != null
    btnCancel.isEnabled = false
    progressBar.visibility = ProgressBar.INVISIBLE
    updateStatus("Выберите файл прошивки")
  }

  private fun showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    Log.d("OTAManager", message)
  }

  override fun onDestroyView() {
    super.onDestroyView()
  }
}