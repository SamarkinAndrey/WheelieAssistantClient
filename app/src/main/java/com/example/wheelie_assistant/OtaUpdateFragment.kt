package com.app.wheelie_assistant

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.wheelie_assistant.OtaManager
import com.google.android.material.button.MaterialButton

class OtaUpdateFragment : Fragment() {
  private lateinit var btnSelectFile: MaterialButton
  private lateinit var btnStartUpdate: MaterialButton
  private lateinit var tvFileName: TextView
  private lateinit var tvFileSize: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var statusBar: TextView

  private lateinit var ssidValue: EditText
  private lateinit var passValue: EditText
  private lateinit var timeoutValue: EditText
  private lateinit var urlValue: EditText
  private lateinit var versionCurrentValue: TextView
  private lateinit var versionActualValue: TextView

  private lateinit var updateLayout: LinearLayout

  private var otaManager: OtaManager? = null
  private var prefManager: PreferencesManager? = null

  private var versionCurrent: String? = null
  private var versionActual: String? = null

//  private var fileUri: Uri? = null
//  private var fileName: String? = null
//  private var fileSize: Long? = null

//  private val selectFirmwareLauncher = registerForActivityResult(
//    ActivityResultContracts.GetContent()
//  ) { uri: Uri? ->
//    uri?.let {
//      fileUri = it
//      readFileInfo()
//    }
//  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_ota_update, container, false)

    App.mainActivity?.let {
      otaManager = it.otaManager
      prefManager = it.prefManager
    }

    initViews(view)
    setup()

    return view
  }

  private fun initViews(view: View) {
    btnSelectFile = view.findViewById(R.id.btnSelectFile)
    btnStartUpdate = view.findViewById(R.id.btnStartUpdate)
    tvFileName = view.findViewById(R.id.tvFileName)
    tvFileSize = view.findViewById(R.id.tvFileSize)
    progressBar = view.findViewById(R.id.progressBar)
    statusBar = view.findViewById(R.id.tvStatus)

    ssidValue = view.findViewById(R.id.ssidValue)
    passValue = view.findViewById(R.id.passValue)
    timeoutValue = view.findViewById(R.id.timeoutValue)
    urlValue = view.findViewById(R.id.urlValue)
    versionCurrentValue = view.findViewById(R.id.versionCurrent)
    versionActualValue = view.findViewById(R.id.versionActual)

    updateLayout = view.findViewById(R.id.updateLayout)
  }

  private fun setup() {
//    btnSelectFile.setOnClickListener {
//      selectFirmwareLauncher.launch("*/*")
//    }

    btnStartUpdate.setOnClickListener {
      otaManager?.requestUpdate(
        ssidValue.text.toString(),
        passValue.text.toString(),
        wifiCallback,
        otaCallback
      )
    }

    prefManager?.let {
      ssidValue.setText(it.load("wifi_ssid"))
      passValue.setText(it.load("wifi_pass"))
    }

    versionCurrent = SettingsManager.currentSettings.firmware_ver
    versionCurrentValue.text = versionCurrent ?: "ошибка"

    checkActualVersion()
  }

//  private fun readFileInfo() {
//    fileUri?.let { uri ->
//      try {
//        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
//          if (cursor.moveToFirst()) {
//            fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
//            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
//            updateFileInfo()
//          }
//        }
//      } catch (e: Exception) {
//        showToast("Ошибка чтения файла: ${e.message}")
//      }
//    }
//  }

//  private fun updateFileInfo() {
//    tvFileName.text = "Файл: $fileName"
//    tvFileSize.text = "Размер: ${fileSize?.let { String.format("%.1f", it / 1024f) } ?: 0} КБ"
//    btnStartUpdate.isEnabled = true
//    updateStatus("Файл выбран. Нажмите «Начать обновление»")
//  }

  private val wifiCallback = object : OtaManager.IWifiCallback {
    override fun onConnecting() {
      btnStartUpdate.isVisible = false
      progressBar.isVisible = true
      setStatus("Подключение к ${ssidValue.text}...")
    }

    override fun onConnected() {
      prefManager?.let {
        it.save("wifi_ssid", ssidValue.text.toString())
        it.save("wifi_pass", passValue.text.toString())
      }
      setStatus("Подключено к ${ssidValue.text}")
    }

    override fun onError() {
      btnStartUpdate.isVisible = true
      progressBar.isVisible = false
      setStatus("Ошибка подключения")
    }
  }

  private val otaCallback = object : OtaManager.IOtaCallback {
    override fun onStarted() {
      setStatus("Обновление прошивки...")
    }

    override fun onProgress(progress: Int) {
      setProgress(progress)
    }

    override fun onSuccess() {
      setStatus("Успешно обновлено, перезагрузка...")
    }

    override fun onFailed(message: String) {
      setStatus("Ошибка обновления: $message")
    }

    override fun onNotify(message: String) {
      setStatus(message)
    }
  }

  private fun setStatus(text: String) {
    statusBar.text = text

    if (!statusBar.isVisible)
      statusBar.isVisible = true
  }

  private fun setProgress(progress: Int) {
    progressBar.progress = progress

    if (!progressBar.isVisible)
      progressBar.isVisible = true
  }

  fun checkActualVersion() {
    versionActualValue.text = "запрос"
    otaManager?.getRemoteVersion { version ->
      versionActual = version
      versionActualValue.text = versionActual ?: "ошибка"

      compareVersions()
    }
  }

  private fun compareVersions() {
    if (!versionCurrent.isNullOrEmpty() &&
      !versionActual.isNullOrEmpty() &&
      otaManager != null// &&
    //otaManager!!.compareVersions(versionActual!!, versionCurrent!!) != 0
    ) {
      versionActualValue.setTextColor(requireContext().getColor(R.color.red))
      updateLayout.isVisible = true
      progressBar.isVisible = false
      statusBar.isVisible = false
      progressBar.progress = 0
      btnStartUpdate.isVisible = true
    } else {
      updateLayout.isVisible = false
      versionActualValue.setTextColor(requireContext().getColor(R.color.white))
    }
  }

  private fun showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    Log.d("OTAManager", message)
  }
}