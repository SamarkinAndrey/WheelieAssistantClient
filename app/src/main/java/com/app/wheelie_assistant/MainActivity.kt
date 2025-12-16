package com.app.wheelie_assistant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.widget.ProgressBar
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.card.MaterialCardView
import java.util.Locale
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import kotlin.math.abs
import kotlin.math.round
import com.app.wheelie_assistant.BTParam.*

class MainActivity : AppCompatActivity(), AppBleScanManager.ICallback {
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

  private lateinit var tvChipTemp: TextView

  private lateinit var positionCard: MaterialCardView
  private lateinit var voltageCard: MaterialCardView

  private lateinit var progressThrottleIn: ProgressBar
  private lateinit var progressThrottleOut: ProgressBar

  private var roll: Float = 0.0f
  private var pitch: Float = 0.0f

  private var voltageIn: Float = 0f
  private var voltageOut: Float = 0f

  private var chipTemp: Int = 0

  private var voltageMin: Float = Float.POSITIVE_INFINITY
  private var voltageMax: Float = Float.NEGATIVE_INFINITY

  private var settingsRequested = false

  private var isActivityVisible = false

  private var systemState: SystemState = SystemState.IDLE
    set(value) {
      field = value
      updateSystemState()
    }

  private val stateIsIdle: Boolean
    get() = systemState == SystemState.IDLE
  private val stateIsMonitoring: Boolean
    get() = systemState == SystemState.MONITORING
  private val stateIsWheelie: Boolean
    get() = systemState == SystemState.WHEELIE
  private val stateIsEmergency: Boolean
    get() = systemState == SystemState.EMERGENCY

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
  private val isDisconnected: Boolean
    get() = connectionState == ConnectionState.DISCONNECTED

  private val handler = Handler(Looper.getMainLooper())
  private val PERMISSION_REQUEST_CODE = 123

  private val CHIP_TEMP_MIN = 55
  private val CHIP_TEMP_MAX = 85

  private val bleManager: AppBleManager
    get() = (application as App).bleManager

  private val scanManager: AppBleScanManager
    get() = (application as App).scanManager

  private val otaManager: AppOtaManager
    get() = (application as App).otaManager

  private val prefManager: AppPrefsManager
    get() = (application as App).prefManager

  private val bluetoothManager by lazy {
    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
  }

  private val bluetoothAdapter: BluetoothAdapter?
    get() = bluetoothManager.adapter

  private val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

  private val btStateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
        when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
          BluetoothAdapter.STATE_ON -> {
            Log.d("MainActivity", "BluetoothAdapter.STATE_ON")
            bleManager.reset()
            startBleScan()
          }
          BluetoothAdapter.STATE_OFF -> {
            Log.d("MainActivity", "BluetoothAdapter.STATE_OFF")
            bleManager.reset()
            scanManager.stopScan()
            onDisconnected()
          }
        }
      }
    }
  }

  private val bluetoothEnableLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == RESULT_OK) {
      startBleScan()
    } else {
      showToast("Bluetooth is required to connect to devices")
    }
  }

  override fun onResume() {
    super.onResume()

    isActivityVisible = true
    Log.d("MainActivity", "Activity resumed")

    if (!isConnected && !isConnecting) {
      Log.d("MainActivity", "Attempting reconnection after resume")
      startBleScan()
    }
  }

  override fun onPause() {
    super.onPause()

    isActivityVisible = false
    Log.d("MainActivity", "Activity paused")
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
    if (message.isNullOrEmpty())
      return

    handler.post { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    App.mainActivity = this

    registerReceiver(btStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

    initViews()
    initProgressBars()
    setupBleManager()

    clearAll()

    startBleScan()
  }

  private fun setupBleManager() {
    bleManager.onRead = { parser ->
      processData(parser)
    }
    bleManager.onState = { state ->
      handler.post {
        when (state) {
          "READY" -> onConnected()
          "DISCONNECTED" -> onDisconnected()
          "CONNECTING" -> onConnecting()
          "CONNECTION_LOST" -> onConnectionLost()
        }
      }
    }
    bleManager.onWriteError = { message ->
      handler.post {
        showToast(message)

        Log.e("MainActivity", "Write error: $message")
      }
    }
  }

  override fun onDeviceFound(device: BluetoothDevice, rssi: Int) {
    Log.d("MainActivity", "Device found: ${device.address}")

    scanManager.stopScan()

    bleManager.connect(device)
      .retry(3, 100)
      .useAutoConnect(false)
      .enqueue()
  }

  override fun onScanStarted() {
    Log.d("MainActivity", "Scan started")

    connectionState = ConnectionState.CONNECTING
  }

  override fun onScanStopped() {
    Log.d("MainActivity", "Scan stopped")
  }

  override fun onScanFailed(errorCode: Int) {
    Log.e("MainActivity", "Scan failed: $errorCode")

    bleManager.reset()

    connectionState = ConnectionState.DISCONNECTED

    startBleScan(1000)
  }

  private fun isBluetoothEnabled(): Boolean {
    return bluetoothAdapter?.isEnabled == true
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

  private fun clearChipTemp(updateView: Boolean = true) {
    chipTemp = 0

    if (updateView)
      updateChipTemp()
  }

  private fun clearVoltage() {
    voltageMin = Float.POSITIVE_INFINITY
    voltageMax = Float.NEGATIVE_INFINITY
  }

  private fun clearAll() {
    clearAttitudeValues()
    clearVoltageValues()
    clearChipTemp()
    clearVoltage()
  }

  private fun initViews() {
    tvThrottleIn = findViewById(R.id.tvThrottleIn)
    tvThrottleOut = findViewById(R.id.tvThrottleOut)
    tvChipTemp = findViewById(R.id.tvChipTemp)
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
        if (hasAllPermissions() && isBluetoothEnabled()) {
          startBleScan()
        } else if (!isBluetoothEnabled()) {
          requestEnableBluetooth()
        } else {
          requestBluetoothPermissions()
        }
      }
    }
    updateConnectionStatus()
  }

  private fun requestEnableBluetooth() {
    val adapter = bluetoothAdapter ?: run {
      showToast("Bluetooth не поддерживается на этом устройстве")
      return
    }

    if (!adapter.isEnabled) {
      val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      bluetoothEnableLauncher.launch(enableBtIntent)
    } else {
      startBleScan()
    }
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
      }
    }
    SettingsActivity.setSaveCallback { parser ->
      parser.setInt(B_SET_SETTINGS, 1)
      bleManager.send(parser)
    }
  }

  private fun setupEnabled() {
    controllerEnabled.setOnClickListener {
      if (!isConnected)
        return@setOnClickListener

      bleManager.send(
        "${B_SET_STATE}=${
          if (stateIsIdle)
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
        getColor(
          if (stateIsIdle)
            R.color.holo_red_light
          else
            R.color.holo_green_light
        )
      )
      wheelieIndicator.isVisible = stateIsWheelie || stateIsEmergency
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

      // avoid division by zero
      val denom = (voltageMax - voltageMin).let { if (it == 0f || it.isInfinite() || it.isNaN()) 1f else it }

      val normalizedThrottleIn = (voltageIn - voltageMin) / denom
      val normalizedThrottleOut = (voltageOut - voltageMin) / denom

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

  private fun updateChipTemp() {
    handler.post {
      tvChipTemp.text = "$chipTemp°C"

      tvChipTemp.setTextColor(
        when (chipTemp) {
          in 1..< CHIP_TEMP_MIN -> {
            getColor(R.color.holo_green_light)
          }
          in CHIP_TEMP_MIN..< CHIP_TEMP_MAX -> {
            getGradientColor(
              position = (chipTemp - CHIP_TEMP_MIN).toFloat() /
                (CHIP_TEMP_MAX - CHIP_TEMP_MIN).toFloat(),
              startColor = getColor(R.color.holo_green_light),
              endColor = getColor(R.color.holo_red_light))
          }
          else -> {
            getColor(R.color.holo_red_light)
          }
        }
      )
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
      try {
        bar.progressDrawable.setTint(color)
      } catch (_: Exception) { /* ignore */ }
    }
  }

  private fun getGradientColor(
    position: Float,
    startColor: Int? = null,
    endColor: Int? = null,
    startHue: Float = 120f,
    endHue: Float = 0f,
    saturation: Float = 1f,
    value: Float = 1f
  ): Int {
    val pos = position.coerceIn(0f, 1f)

    return if (startColor != null && endColor != null) {
      val startA = Color.alpha(startColor)
      val startR = Color.red(startColor)
      val startG = Color.green(startColor)
      val startB = Color.blue(startColor)

      val endA = Color.alpha(endColor)
      val endR = Color.red(endColor)
      val endG = Color.green(endColor)
      val endB = Color.blue(endColor)

      val alpha = (startA + (endA - startA) * pos).toInt()
      val red = (startR + (endR - startR) * pos).toInt()
      val green = (startG + (endG - startG) * pos).toInt()
      val blue = (startB + (endB - startB) * pos).toInt()

      Color.argb(alpha, red, green, blue)
    } else {
      val hue = startHue + (endHue - startHue) * pos

      Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
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
        }
        cur >= max -> {
          wheelieIndicator.setColorFilter(wheelieIndicator.context.getColor(R.color.red))
          wheelieIndicator.alpha = 1f
        }
      }
    }
  }

  private fun updateAll() {
    updateAttitudeView()
    updateVoltageDisplays()
    updateChipTemp()
    updateWheelieIndicator()
  }

  private fun requestBluetoothPermissions() {
    if (hasAllPermissions())
      return

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
    if (isBluetoothEnabled()) {
      startBleScan()
    } else {
      requestEnableBluetooth()
    }
  }

  private fun onPermissionsDenied() {
    showToast("Bluetooth permissions required")
  }

  private fun disconnectManually() {
    Log.d("MainActivity", "Manual disconnect initiated")
    connectionState = ConnectionState.DISCONNECTED
    try {
      scanManager.stopScan()
      bleManager.disconnect().enqueue()
      clearUI()
    } catch (e: Exception) {
      Log.e("MainActivity", "disconnectManually error: ${e.message}")
      clearUI()
    }
  }

  private fun clearUI() {
    Log.d("MainActivity", "clearUI()")
    systemState = SystemState.IDLE
    SettingsManager.clear()
    App.settingsActivity?.finish()
    progressFinish()
    clearAll();
    unlockScreen()
  }

  private fun onConnected() {
    if (isConnected)
      return

    Log.d("MainActivity", "onConnected()")
    lockScreen()
    connectionState = ConnectionState.CONNECTED
    bleManager.send("${B_CONNECTED}=1")
  }

  private fun onConnecting() {
    if (isConnecting)
      return

    Log.d("MainActivity", "onConnecting()")
    connectionState = ConnectionState.CONNECTING
  }

  private fun onConnectionLost() {
    Log.e("MainActivity", "onConnectionLost()")

    onDisconnected()

    startBleScan(1000)
  }

  private fun startBleScan(delay: Long = 0) {
    if (isConnecting || isConnected) {
      Log.d("MainActivity", "Already connecting/connected, skipping scan")
      return
    }

    handler.postDelayed({
      if (!isActivityVisible) {
        Log.d("MainActivity", "Activity not visible, skipping scan")
        return@postDelayed
      }

      if (hasAllPermissions() && isBluetoothEnabled()) {
        Log.d("MainActivity", "Starting BLE scan")
        scanManager.startScan()
      } else {
        Log.w("MainActivity", "Cannot start scan - bluetooth permissions required")
      }
    }, delay)
  }

//  private fun startBleScan(delay: Long = 0) {
//    handler.postDelayed({
//      if (hasAllPermissions() && isBluetoothEnabled())
//        scanManager.startScan()
//    }, delay)
//  }

  private fun onDisconnected() {
    if (isDisconnected)
      return

    clearUI()
    connectionState = ConnectionState.DISCONNECTED
  }

  private fun updateConnectionStatus() {
    handler.post {
      when (connectionState) {
        ConnectionState.CONNECTED -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_connected_24px)
          connectionIndicator.setColorFilter(
            getColor(R.color.holo_green_light)
          )
        }
        ConnectionState.CONNECTING -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_searching_24px)
          connectionIndicator.setColorFilter(
            getColor(R.color.holo_blue_light)
          )
        }
        ConnectionState.DISCONNECTED -> {
          connectionIndicator.setImageResource(R.drawable.bluetooth_disabled_24px)
          connectionIndicator.setColorFilter(
            getColor(R.color.holo_red_light)
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
      pitch = round(params.getFloat(B_PITCH, pitch) * 10) / 10
      roll = round(params.getFloat(B_ROLL, roll) * 10) / 10
      voltageIn = params.getFloat(B_VOLTAGE_IN, voltageIn)
      voltageOut = params.getFloat(B_VOLTAGE_OUT, voltageOut)
      chipTemp = params.getInt(B_CHIP_TEMP, chipTemp)
    }

    if (params.getInt(B_GET_SETTINGS) == 1) {
      SettingsManager.loadFrom(params)
      if (settingsRequested) {
        settingsRequested = false
        openSettings()
      }
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

    updateAll()
  }

  private fun openSettings() {
    val intent = Intent(this, SettingsActivity::class.java)
    startActivity(intent)
  }

  override fun onDestroy() {
    super.onDestroy()

    if (App.mainActivity === this)
      App.mainActivity = null

    try {
      unregisterReceiver(btStateReceiver)
    } catch (e: Exception) {
      Log.d("MainActivity", "btStateReceiver unregister failed: ${e.message}")
    }
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