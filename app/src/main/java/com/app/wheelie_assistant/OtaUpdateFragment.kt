package com.app.wheelie_assistant

import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton

class OtaUpdateFragment : InfoFragment() {
  private lateinit var btnSelectFile: MaterialButton
  private lateinit var btnStartUpdate: MaterialButton
  private lateinit var tvFileName: TextView
  private lateinit var tvFileSize: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var statusBar: TextView

  private lateinit var ssidValue: AppCompatEditText
  private lateinit var passValue: AppCompatEditText
  private lateinit var timeoutValue: EditText
  private lateinit var urlValue: EditText
  private lateinit var currentVersionValue: TextView
  private lateinit var actualVersionValue: TextView

  private lateinit var togglePassword: AppCompatImageButton

  private lateinit var updateLayout: LinearLayout

  private var otaManager: AppOtaManager? = null
  private var prefManager: AppPrefsManager? = null

  override fun onPause() {
    super.onPause()
    isPasswordVisible = false
  }

  private var currentVersion: String? = null
    set(value) {
      field = value

      currentVersionValue.text = value ?: "ошибка"
      currentVersionValue.setTextColor(
        requireContext().getColor(
          if (value.isNullOrBlank())
            R.color.red
          else
            R.color.white
        )
      )
    }

  private var actualVersion: String? = null

  private fun checkIsNewVersion() {
    val isNewVersion = !currentVersion.isNullOrBlank() &&
                                !actualVersion.isNullOrBlank() &&
                                otaManager != null // &&
                                // otaManager!!.compareVersions(versionActual!!, versionCurrent!!) != 0

    updateLayout.post {
      if (isNewVersion) {
        progressBar.isVisible = false
        statusBar.isVisible = false
        progressBar.progress = 0
        btnStartUpdate.isVisible = true
      }

      actualVersionValue.text = actualVersion ?: "ошибка"
      actualVersionValue.setTextColor(
        requireContext().getColor(
          if (isNewVersion)
            R.color.green
          else
            if (actualVersion.isNullOrBlank())
              R.color.red
            else
              R.color.white
        )
      )

      updateLayout.isVisible = isNewVersion
    }
  }

  private var isPasswordVisible: Boolean
    get() = passValue.transformationMethod !is PasswordTransformationMethod
    set(value) {
      val selection = passValue.selectionEnd

      passValue.transformationMethod =
        if (value) null else PasswordTransformationMethod.getInstance()

      togglePassword.setImageResource(
        if (value)
          R.drawable.eye_outline
        else
          R.drawable.eye_off_outline
      )
      passValue.setSelection(selection)
    }

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

  override fun getFragmentID(): Int = R.layout.fragment_ota_update

  override fun onInit(view: View) {
    App.mainActivity?.let {
      otaManager = it.otaManager
      prefManager = it.prefManager
    }

    initViews(view)
    setup()

    isPasswordVisible = false
  }

  override fun onLoadSettings(settings: Settings) {
    currentVersion = settings.firmware_ver.ifBlank { null }
    checkActualVersion()
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
    currentVersionValue = view.findViewById(R.id.versionCurrent)
    actualVersionValue = view.findViewById(R.id.versionActual)
    togglePassword = view.findViewById(R.id.togglePassword)

    updateLayout = view.findViewById(R.id.updateLayout)
  }

  private fun setup() {
//    btnSelectFile.setOnClickListener {
//      selectFirmwareLauncher.launch("*/*")
//    }

    togglePassword.setOnClickListener {
      isPasswordVisible = !isPasswordVisible
    }

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

  private val wifiCallback = object : AppOtaManager.IWifiCallback {
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

  private val otaCallback = object : AppOtaManager.IOtaCallback {
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
    actualVersion = null
    actualVersionValue.text = "запрос"
    actualVersionValue.setTextColor(requireContext().getColor(R.color.blue))

    otaManager?.getRemoteVersion { version ->
      actualVersion = version
      checkIsNewVersion()
    }
  }

  private fun showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    Log.d("OTAManager", message)
  }
}