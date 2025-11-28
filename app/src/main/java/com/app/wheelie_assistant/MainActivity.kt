package com.app.wheelie_assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import kotlin.math.abs
import kotlin.math.round
import android.widget.ProgressBar
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.card.MaterialCardView
import java.util.Locale
import JsonParamParser
import BTParam.*
import android.app.Application
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible

class App : Application() {
  companion object {
    var mainActivity: MainActivity? = null
    var settingsActivity: SettingsActivity? = null
  }
}

class MainActivity : AppCompatActivity() {
  enum class SystemState {
    IDLE,
    MONITORING,
    WHEELIE,
    EMERGENCY;

    override fun toString(): String = this.ordinal.toString()
    fun toInt(): Int = this.ordinal

    companion object {
      fun fromInt(value: Int): SystemState? = entries.getOrNull(value)
    }
  }

  private lateinit var settingsButton: AppCompatImageButton
  private lateinit var connectionIndicator: AppCompatImageButton
  private lateinit var controllerEnabled: AppCompatImageButton
  private lateinit var wheelieIndicator: AppCompatImageView

  private lateinit var pitchValue: TextView
  private lateinit var rollValue: TextView

  private lateinit var pitchIndicatorUp: AppCompatImageView
  private lateinit var pitchIndicatorDown: AppCompatImageView

  private lateinit var rollIndicatorLeft: AppCompatImageView
  private lateinit var rollIndicatorRight: AppCompatImageView

  private lateinit var positionView: PositionView
  private lateinit var tvThrottleIn: TextView
  private lateinit var tvThrottleOut: TextView

  private lateinit var positionCard: MaterialCardView
  private lateinit var voltageCard: MaterialCardView

  private lateinit var progressThrottleIn: ProgressBar
  private lateinit var progressThrottleOut: ProgressBar

  private var roll: Float = 0.0f
  private var pitch: Float = 0.0f

  private var voltageIn: Float = 0f
  private var voltageOut: Float = 0f

  private var voltageMin: Float = Float.POSITIVE_INFINITY
  private var voltageMax: Float = Float.NEGATIVE_INFINITY

  private val PREFS_NAME = "Bluetooth"
  private val KEY_LAST_MAC = "last_mac_address"

  private var settingsRequested = false;

  private var systemState: SystemState = SystemState.IDLE
    set(value) {
      field = value
      updateSystemState()
    }

  private fun stateIsIdle(): Boolean = systemState == SystemState.IDLE
  private fun stateIsMonitoring(): Boolean = systemState == SystemState.MONITORING
  private fun stateIsWheelie(): Boolean = systemState == SystemState.WHEELIE
  private fun stateIsEmergency(): Boolean = systemState == SystemState.EMERGENCY

  private enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED
  }

  private var connectionState = ConnectionState.DISCONNECTED
    set(value) {
      field = value
      updateConnectionStatus()
    }

  private val isConnecting: Boolean
    get() = connectionState == ConnectionState.CONNECTING

  private val isConnected: Boolean
    get() = connectionState == ConnectionState.CONNECTED

  private var serverMac: String? = null

  private val handler = Handler(Looper.getMainLooper())
  private val PERMISSION_REQUEST_CODE = 123
  private val BLUETOOTH_ENABLE_REQUEST_CODE = 124

  lateinit var bleManager: BleManager
  lateinit var otaManager: OtaManager
  lateinit var prefManager: PrefsManager

  val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
  } else {
    arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
  }

  private fun lockScreen() {
    handler.post {
      window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  private fun unlockScreen() {
    handler.post {
      window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  private fun showToast(message: String?) {
    if (message.isNullOrEmpty()) return
    handler.post { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    App.mainActivity = this

    initViews()
    initProgressBars()
    setupBleManager()
    setupOtaManager()
    setupPrefManager()
    clearAttitudeValues()
    clearVoltageValues()
    clearVoltage()

    if (hasAllPermissions())
      bleManager.startAutoConnect()
  }

  private fun setupBleManager() {
    bleManager = BleManager(this)
    bleManager.setDataCallback { parser ->
      processData(parser)
    }
    bleManager.setConnectionCallback { connectionStatus ->
      handler.post {
        when (connectionStatus) {
          "READY" -> onConnected()
          "DISCONNECTED" -> onDisconnected()
          "CONNECTING" -> onConnecting()
          "CONNECTION_LOST" -> onConnectionLost()
        }
      }
    }
    bleManager.setWriteErrorCallback { errorMessage ->
      handler.post {
        showToast(errorMessage)
        Log.e("MainActivity", "Write error: $errorMessage")
      }
    }
  }

  private fun setupOtaManager() {
    otaManager = OtaManager(this, bleManager)
  }

  private fun setupPrefManager() {
    prefManager = PrefsManager(this)
  }

  private fun clearAttitudeValues(updateView: Boolean = true) {
    roll = 0f
    pitch = 0f

    if (updateView)
      updateAttitudeView()
  }

  private fun clearVoltageValues(updateView: Boolean = true) {
    voltageIn = 0f
    voltageOut = 0f

    if (updateView)
      updateVoltageDisplays()
  }

  private fun clearVoltage() {
    voltageMin = Float.POSITIVE_INFINITY
    voltageMax = Float.NEGATIVE_INFINITY
  }

//  private fun connectToLastDevice() {
//    serverMac = loadLastMacAddress()
//    if (!serverMac.isNullOrEmpty()) {
//      connectToServer()
//    }
//  }

  private fun initViews() {
    tvThrottleIn = findViewById(R.id.tvThrottleIn)
    tvThrottleOut = findViewById(R.id.tvThrottleOut)
    progressThrottleIn = findViewById(R.id.progressVoltageIn)
    progressThrottleOut = findViewById(R.id.progressVoltageOut)
    positionView = findViewById(R.id.positionView)
    pitchValue = findViewById(R.id.pitchValue)
    rollValue = findViewById(R.id.rollValue)

    pitchIndicatorUp = findViewById(R.id.pitch_indicator_up)
    pitchIndicatorDown = findViewById(R.id.pitch_indicator_down)

    rollIndicatorLeft = findViewById(R.id.roll_indicator_left)
    rollIndicatorRight = findViewById(R.id.roll_indicator_right)

    settingsButton = findViewById(R.id.settings_button)
    connectionIndicator = findViewById(R.id.connectionIndicator)
    controllerEnabled = findViewById(R.id.controllerEnabled)
    wheelieIndicator = findViewById(R.id.wheelieIndicator)

    positionCard = findViewById(R.id.positionCard)
    voltageCard = findViewById(R.id.voltageCard)

    setupConnectionIndicator()
    setupPositionCard()
    setupVoltageCard()
    setupSettings()
    setupEnabled()
  }

  private fun initProgressBars() {
    progressThrottleIn.max = 100
    progressThrottleIn.progress = 0
    progressThrottleOut.max = 100
    progressThrottleOut.progress = 0
  }

  private fun setupPositionCard() {
    positionCard.setOnLongClickListener {
      if (isConnected) {
        showConfirmation(message = "Начать калибровку гироскопа?", onPositive = {
          bleManager.send("${B_CALIBRATE_GYRO}=1")
        })
      }
      true
    }
  }

  private fun setupVoltageCard() {
    voltageCard.setOnLongClickListener {
      if (isConnected) {
        showConfirmation(message = "Сбросить вольтаж?", onPositive = {
          bleManager.send("${B_RESET_VOLTAGE}=1")
        })
      }
      true
    }
  }

  private fun setupConnectionIndicator() {
    connectionIndicator.setOnClickListener {
      if (isConnected || isConnecting) {
        disconnectManually()
      } else {
        if (hasAllPermissions())
          bleManager.startAutoConnect()
        else
          requestBluetoothPermissions()
      }
    }
    updateConnectionStatus()
  }

  private fun setupSettings() {
    settingsButton.setOnClickListener {
      if (isConnected) {
        if (SettingsManager.isLoaded())
          openSettings()
        else {
          bleManager.send("${B_GET_SETTINGS}=1")

          settingsRequested = true
          handler.postDelayed({ settingsRequested = false }, 1000)
        }
//      showToast("Settings requested")
      }
    }
    SettingsActivity.setSaveCallback { parser ->
      parser.setInt(B_SET_SETTINGS, 1)
      bleManager.send(parser)
//      showToast("Settings sended")
    }
  }

  private fun setupEnabled() {
    controllerEnabled.setOnClickListener {
      if (!isConnected)
        return@setOnClickListener

      bleManager.send(
        "${B_SET_STATE}=${
          if (stateIsIdle())
            SystemState.MONITORING
          else
            SystemState.IDLE
        }"
      )
    }
  }

  private fun updateSystemState() {
    handler.post {
      controllerEnabled.setColorFilter(
        ContextCompat.getColor(
          this,
          if (stateIsIdle())
            R.color.holo_red_light
          else
            R.color.holo_green_light
        )
      )

      wheelieIndicator.isVisible = stateIsWheelie() || stateIsEmergency()
    }
  }

  private fun updateAttitudeView() {
    handler.post {
      positionView.roll = roll
      positionView.pitch = pitch

      rollValue.text = "${"%.1f".format(Locale.US, abs(roll))}°"
      pitchValue.text = "${"%.1f".format(Locale.US, abs(pitch))}°"

      if (roll < 0) {
        rollIndicatorLeft.setImageDrawable(null)
        rollIndicatorRight.setImageResource(R.drawable.arrow_right_40px)
      } else if (roll > 0) {
        rollIndicatorLeft.setImageResource(R.drawable.arrow_left_40px)
        rollIndicatorRight.setImageDrawable(null)
      } else {
        rollIndicatorLeft.setImageDrawable(null)
        rollIndicatorRight.setImageDrawable(null)
      }

      if (pitch < 0) {
        pitchIndicatorUp.setImageDrawable(null)
        pitchIndicatorDown.setImageResource(R.drawable.arrow_drop_down_48px)
      } else if (pitch > 0) {
        pitchIndicatorUp.setImageResource(R.drawable.arrow_drop_up_48px)
        pitchIndicatorDown.setImageDrawable(null)
      } else {
        pitchIndicatorUp.setImageDrawable(null)
        pitchIndicatorDown.setImageDrawable(null)
      }
    }
  }

  private fun updateVoltageDisplays() {
    handler.post {
      tvThrottleIn.text = "${String.format("%.2f", voltageIn)}"
      tvThrottleOut.text = "${String.format("%.2f", voltageOut)}"

//      progressThrottleIn.setProgress(
//        ((voltageIn - voltageMin) / (voltageMax - voltageMin) * 100).toInt().coerceIn(0, 100), true
//      )
//      progressThrottleOut.setProgress(
//        ((voltageOut - voltageMin) / (voltageMax - voltageMin) * 100).toInt().coerceIn(0, 100), true
//      )

      val normalizedThrottleIn = (voltageIn - voltageMin) / (voltageMax - voltageMin)
      val normalizedThrottleOut = (voltageOut - voltageMin) / (voltageMax - voltageMin)

      progressThrottleIn.progress = (normalizedThrottleIn * 100).toInt().coerceIn(0, 100)
      progressThrottleOut.progress = (normalizedThrottleOut * 100).toInt().coerceIn(0, 100)

      progressThrottleIn.invalidate()
      progressThrottleOut.invalidate()

      val colorIn = getGradientColor(normalizedThrottleIn)
      val colorOut = getGradientColor(normalizedThrottleOut)

      updateProgressBarColor(progressThrottleIn, colorIn)
      updateProgressBarColor(progressThrottleOut, colorOut)
    }
  }

  private fun updateProgressBarColor(bar: ProgressBar, color: Int) {
    try {
      val progressDrawable = bar.progressDrawable.mutate()
      if (progressDrawable is LayerDrawable) {
        val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)
        progressLayer?.setTint(color)
      } else {
        progressDrawable.setTint(color)
      }
      bar.progressDrawable = progressDrawable
    } catch (e: Exception) {
      bar.progressDrawable.setTint(color)
    }
  }

  private fun getGradientColor(position: Float): Int {
    val hue = 120 * (1 - position.coerceIn(0f, 1f))
    return Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
  }

  fun calculateAlpha(min: Float, mid: Float, max: Float, cur: Float): Float {
    return when {
      cur <= min -> 0f
      cur >= max -> 0f
      cur <= mid -> {
        (cur - min) / (mid - min)
      }

      else -> {
        1f - (cur - mid) / (max - mid)
      }
    }
  }

  fun updateWheelieIndicator() {
    if (!wheelieIndicator.isVisible)
      return

    val settings = SettingsManager.settings

    val min = settings.target_pitch - settings.exit_threshold
    val mid = settings.target_pitch
    val max = settings.target_pitch + settings.emerg_threshold
    val cur = pitch

    handler.post {
      when {
        cur < min -> {
          wheelieIndicator.setColorFilter(Color.TRANSPARENT)
          wheelieIndicator.alpha = 0f
//          wheelieIndicator.isVisible = false
        }

        cur in min..<mid -> {
          val progress = (cur - min) / (mid - min)
          val color = ColorUtils.blendARGB(
            wheelieIndicator.context.getColor(R.color.yellow),
            wheelieIndicator.context.getColor(R.color.green),
            progress
          )
          wheelieIndicator.setColorFilter(color)
          wheelieIndicator.alpha = 1f
//          wheelieIndicator.alpha = calculateAlpha(min, mid, max, cur)
//          wheelieIndicator.isVisible = true
        }

        cur in mid..<max -> {
          val progress = (cur - mid) / (max - mid)
          val color = ColorUtils.blendARGB(
            wheelieIndicator.context.getColor(R.color.green),
            wheelieIndicator.context.getColor(R.color.red),
            progress
          )
          wheelieIndicator.setColorFilter(color)
          wheelieIndicator.alpha = 1f
//          wheelieIndicator.isVisible = true
        }

        cur >= max -> {
          wheelieIndicator.setColorFilter(wheelieIndicator.context.getColor(R.color.red))
          wheelieIndicator.alpha = 1f
//          wheelieIndicator.isVisible = true
        }
      }
    }
  }

  private fun requestBluetoothPermissions() {
    if (hasAllPermissions()) return
    ActivityCompat.requestPermissions(this, bluetoothPermissions, PERMISSION_REQUEST_CODE)
  }

  private fun hasAllPermissions(): Boolean {
    return bluetoothPermissions.all { permission ->
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<out String>, grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    when (requestCode) {
      PERMISSION_REQUEST_CODE -> {
        if (hasAllPermissions()) {
          onPermissionsGranted()
        } else {
          onPermissionsDenied()
        }
      }
    }
  }

  private fun onPermissionsGranted() {
    showToast("Bluetooth permissions granted")

    bleManager.startAutoConnect()

//    if (!serverMac.isNullOrEmpty()) {
//      //connectToServer()
//    } else {
//      bleManager.startAutoConnect()
//    }
  }

  private fun onPermissionsDenied() {
    showToast("Bluetooth permissions required")

//    connectionState = ConnectionState.DISCONNECTED
  }

  private fun saveLastMacAddress(macAddress: String?) {
    macAddress?.let { prefManager.save(KEY_LAST_MAC, macAddress) }
  }

  private fun loadLastMacAddress(): String? {
    return prefManager.load(KEY_LAST_MAC).ifEmpty { null }
  }

  private fun clearLastMacAddress() {
    prefManager.remove(KEY_LAST_MAC)
  }

  private fun disconnectManually() {
    Log.d("MainActivity", "Manual disconnect initiated")
    closeConnection()
    bleManager.disconnectDevice()
  }

  private fun normalizeMacAddress(mac: String): String? {
    val cleanMac = mac.replace("[:\\-\\.]".toRegex(), "").uppercase()

    if (!cleanMac.matches("[0-9A-F]{12}".toRegex())) {
      return null
    }

    return cleanMac.chunked(2).joinToString(":")
  }

  private fun closeConnection() {
    systemState = SystemState.IDLE

    SettingsManager.clear()

    App.settingsActivity?.finish()
//    App.settingsActivity?.loadSettings()

    progressFinish()

    clearAttitudeValues()
    clearVoltageValues()
    clearVoltage()
    unlockScreen()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        handler.post { bleManager.startAutoConnect() }
      } else {
        showToast("Bluetooth is required to connect to devices")
      }
    }
  }

  private fun onConnected() {
    if (connectionState == ConnectionState.CONNECTED)
      return

    saveLastMacAddress(serverMac)
    lockScreen()
    connectionState = ConnectionState.CONNECTED

    bleManager.send("${B_CONNECTED}=1")
  }

  private fun onConnecting() {
    if (connectionState == ConnectionState.CONNECTING)
      return

    connectionState = ConnectionState.CONNECTING
  }

  private fun onConnectionLost() {
    closeConnection()
    onConnecting()
  }

  private fun onDisconnected() {
    if (connectionState == ConnectionState.DISCONNECTED)
      return

    closeConnection()
    connectionState = ConnectionState.DISCONNECTED
  }

  private fun updateConnectionStatus() {
    handler.post {
      when (connectionState) {
        ConnectionState.CONNECTED -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_connected_24px)
          connectionIndicator.setColorFilter(
            ContextCompat.getColor(
              this, R.color.holo_green_light
            )
          )
        }

        ConnectionState.CONNECTING -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_searching_24px)
          connectionIndicator.setColorFilter(
            ContextCompat.getColor(
              this, R.color.holo_blue_light
            )
          )
        }

        ConnectionState.DISCONNECTED -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_disabled_24px)
          connectionIndicator.setColorFilter(
            ContextCompat.getColor(
              this, R.color.holo_red_light
            )
          )
        }
      }
    }
  }

  private fun processData(params: JsonParamParser) {
    if (otaManager.isStarted()) {
      otaManager.processUpdate(params)

      return
    }

    if (params.hasParam(B_GET_STATE)) {
      SystemState.fromInt(params.getInt(B_GET_STATE))?.run {
        systemState = this
      }
    }

    if (!progressIsShowing()) {
      pitch = round(params.getFloat(B_PITCH, pitch) * 10) / 10;
      roll = round(params.getFloat(B_ROLL, roll) * 10) / 10;
      voltageIn = params.getFloat(B_VOLTAGE_IN, voltageIn)
      voltageOut = params.getFloat(B_VOLTAGE_OUT, voltageOut)
    }

    if (params.getInt(B_GET_SETTINGS) == 1) {
      SettingsManager.loadFrom(params)

      if (settingsRequested) {
        settingsRequested = false;
        openSettings()
      }

//      App.settingsActivity?.loadSettings()
//        ?: takeIf { settingsRequested }
//          ?.let {
//            settingsRequested = false
//            openSettings()
//          }
    }

    if (params.getInt(B_SET_SETTINGS, 0) == 1)
      showToast("Настройки сохранены")

    if (params.getInt(B_RESET_VOLTAGE, 0) == 1)
      clearVoltage()

    if (params.hasParam(B_VOLTAGE_MIN))
      voltageMin = params.getFloat(B_VOLTAGE_MIN)

    if (params.hasParam(B_VOLTAGE_MAX))
      voltageMax = params.getFloat(B_VOLTAGE_MAX)

    if (params.hasParam(B_CALIBRATE_GYRO)) {
      val value = params.getInt(B_CALIBRATE_GYRO)

      if (value == 1) {
        clearAttitudeValues()

        progressStart(this, "Калибровка гироскопа...", 1000 * 20)
      } else {
        progressFinish()

        bleManager.send("${B_GET_POSITION}=1")

        if (value == -1)
          showToast("Gyroscope calibration failed")
      }
    }

    if (params.hasParam(B_CALIBRATE_GYRO_PROG))
      progressSet(params.getInt(B_CALIBRATE_GYRO_PROG))

    updateAttitudeView()
    updateVoltageDisplays()
    updateWheelieIndicator()
  }

  private fun openSettings() {
    val intent = Intent(this, SettingsActivity::class.java)
    startActivity(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    App.mainActivity = null

    disconnectManually()
  }

  private fun showConfirmation(
    title: String = "",
    message: String,
    onPositive: () -> Unit = {},
    onNegative: () -> Unit = {},
    onDismiss: () -> Unit = {}
  ) {
    showDialog(
      title = title,
      message = message,
      onPositive = onPositive,
      onNegative = onNegative,
      onDismiss = onDismiss
    )
  }

  private fun showDialog(
    title: String = "",
    message: String,
    positiveText: String = "OK",
    negativeText: String = "Отмена",
    isCancelable: Boolean = true,
    onPositive: () -> Unit = {},
    onNegative: () -> Unit = {},
    onDismiss: () -> Unit = {}
  ) {
    MaterialAlertDialogBuilder(this).setTitle(title).setMessage(message)
      .setPositiveButton(positiveText) { dialog, which -> onPositive() }
      .setNegativeButton(negativeText) { dialog, which -> onNegative() }.setCancelable(isCancelable)
      .setOnDismissListener { onDismiss() }.show()
  }
}