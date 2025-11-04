package com.app.wheelie_assistant

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.slider.Slider
import com.google.android.material.slider.RangeSlider
import androidx.appcompat.widget.AppCompatImageButton
import android.widget.LinearLayout
import kotlin.math.max
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
  companion object {
    private var sendCallback: ((String) -> Unit)? = null

    fun setSendCallback(callback: (String) -> Unit) {
      sendCallback = callback
    }
  }

  private lateinit var saveButton: MaterialButton
  private lateinit var backButton: AppCompatImageButton

  private lateinit var systemTickValue: MaterialTextView
  private lateinit var predictionHorizontValue: MaterialTextView
  private lateinit var gyroHysteresisValue: MaterialTextView
  private lateinit var targetPitchValue: MaterialTextView
  private lateinit var deadZoneValue: MaterialTextView
  private lateinit var exitThresholdValue: MaterialTextView
  private lateinit var emergThresholdValue: MaterialTextView
  private lateinit var minVoltageValue: MaterialTextView
  private lateinit var stepRangeValue: MaterialTextView
  private lateinit var hysteresisValue: MaterialTextView
  private lateinit var maxSpeedValue: MaterialTextView
  private lateinit var trendDurationValue: MaterialTextView
  private lateinit var stableDurationValue: MaterialTextView

  private lateinit var systemTickSlider: Slider
  private lateinit var predictionHorizontSlider: Slider
  private lateinit var gyroHysteresisSlider: Slider
  private lateinit var targetPitchSlider: Slider
  private lateinit var deadZoneSlider: Slider
  private lateinit var exitThresholdSlider: Slider
  private lateinit var emergThresholdSlider: Slider
  private lateinit var minVoltageSlider: Slider
  private lateinit var stepRangeSlider: RangeSlider
  private lateinit var hysteresisSlider: Slider
  private lateinit var maxSpeedSlider: Slider
  private lateinit var trendDurationSlider: Slider
  private lateinit var stableDurationSlider: Slider

  private lateinit var reversedPitchValue: SwitchCompat
  private lateinit var reversedRollValue: SwitchCompat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_activity)

    initViews()
    setupSliders()
    setupClickListeners()
    loadSettings(SettingsManager.currentSettings)
  }

  private fun initViews() {
    systemTickValue = findViewById(R.id.system_tick_value)
    predictionHorizontValue = findViewById(R.id.prediction_horizont_value)
    gyroHysteresisValue = findViewById(R.id.gyro_hysteresis_value)
    targetPitchValue = findViewById(R.id.target_pitch_value)
    deadZoneValue = findViewById(R.id.dead_zone_value)
    exitThresholdValue = findViewById(R.id.exit_threshold_value)
    emergThresholdValue = findViewById(R.id.emerg_threshold_value)
    minVoltageValue = findViewById(R.id.min_voltage_value)
    stepRangeValue = findViewById(R.id.step_range_value)
    hysteresisValue = findViewById(R.id.hysteresis_value)
    maxSpeedValue = findViewById(R.id.max_speed_value)
    trendDurationValue = findViewById(R.id.trend_duration_value)
    stableDurationValue = findViewById(R.id.stable_duration_value)

    systemTickSlider = findViewById(R.id.system_tick_slider)
    predictionHorizontSlider = findViewById(R.id.prediction_horizont_slider)
    gyroHysteresisSlider = findViewById(R.id.gyro_hysteresis_slider)
    targetPitchSlider = findViewById(R.id.target_pitch_slider)
    deadZoneSlider = findViewById(R.id.dead_zone_slider)
    exitThresholdSlider = findViewById(R.id.exit_threshold_slider)
    emergThresholdSlider = findViewById(R.id.emerg_threshold_slider)
    minVoltageSlider = findViewById(R.id.min_voltage_slider)
    stepRangeSlider = findViewById(R.id.step_range_slider)
    hysteresisSlider = findViewById(R.id.hysteresis_slider)
    maxSpeedSlider = findViewById(R.id.max_speed_slider)
    trendDurationSlider = findViewById(R.id.trend_duration_slider)
    stableDurationSlider = findViewById(R.id.stable_duration_slider)

    reversedPitchValue = findViewById(R.id.reversed_pitch_value)
    reversedRollValue = findViewById(R.id.reversed_roll_value)

    saveButton = findViewById(R.id.save_button)
    backButton = findViewById(R.id.back_button)

    val scrollLayout = findViewById<LinearLayout>(R.id.scrollLayout)
    val saveLayout = findViewById<LinearLayout>(R.id.saveLayout)

    saveLayout.post {
      scrollLayout.setPadding(
        scrollLayout.paddingLeft,
        scrollLayout.paddingTop,
        scrollLayout.paddingRight,
        saveLayout.height
      )
    }
  }

  private fun setupClickListeners() {
    saveButton.setOnClickListener {
      if (saveSettings())
        finish()
    }

    backButton.setOnClickListener {
      loadSettings(SettingsManager.currentSettings)
      finish()
    }
  }

  private fun updatePredictionHorizont() {
    val valueFrom = systemTickSlider.value

    if (predictionHorizontSlider.value < valueFrom) {
      predictionHorizontSlider.value = valueFrom
      predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"
    }

    predictionHorizontSlider.valueFrom = valueFrom
  }

  private fun setupSliders() {
    systemTickSlider.addOnChangeListener { _, value, _ ->
      systemTickValue.text = "${value.toInt()} мс"
      updatePredictionHorizont()
    }

    predictionHorizontSlider.addOnChangeListener { _, value, _ ->
      predictionHorizontValue.text = "${value.toInt()} мс"
    }

    gyroHysteresisSlider.addOnChangeListener { _, value, _ ->
      gyroHysteresisValue.text = "${"%.1f".format(value)}°"
    }

    targetPitchSlider.addOnChangeListener { _, value, _ ->
      targetPitchValue.text = "${value.toInt()}°"
    }

    deadZoneSlider.addOnChangeListener { _, value, _ ->
      deadZoneValue.text = "${"%.1f".format(value)}°"
    }

    exitThresholdSlider.addOnChangeListener { _, value, _ ->
      exitThresholdValue.text = "${"-%.1f".format(value)}°"
    }

    emergThresholdSlider.addOnChangeListener { _, value, _ ->
      emergThresholdValue.text = "${"+%.1f".format(value)}°"
    }

    minVoltageSlider.addOnChangeListener { _, value, _ ->
      minVoltageValue.text = "${"%.1f".format(value)} В"
    }

    stepRangeSlider.addOnChangeListener { _, value, fromUser ->
      val values = stepRangeSlider.values
      if (values[0] != values[1])
        stepRangeValue.text = "${values[0].toInt()} - ${values[1].toInt()} кОм"
      else
        stepRangeValue.text = "${values[0].toInt()} кОм"
    }

    hysteresisSlider.addOnChangeListener {  _, value, _ ->
      hysteresisValue.text = "${"%.1f".format(value)}°"
    }

    maxSpeedSlider.addOnChangeListener {  _, value, _ ->
      maxSpeedValue.text = "${value.toInt()} °/мс"
    }

    trendDurationSlider.addOnChangeListener {  _, value, _ ->
      trendDurationValue.text = "${value.toInt()} мс"
    }

    stableDurationSlider.addOnChangeListener {  _, value, _ ->
      stableDurationValue.text = "${value.toInt()} мс"
    }
  }

  private fun updateAllTextValues() {
    systemTickValue.text = "${systemTickSlider.value.toInt()} мс"
    predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"
    gyroHysteresisValue.text = "${"%.1f".format(gyroHysteresisSlider.value)}°"
    targetPitchValue.text = "${targetPitchSlider.value.toInt()}°"
    deadZoneValue.text = "${"%.1f".format(deadZoneSlider.value)}°"
    exitThresholdValue.text = "${"-%.1f".format(exitThresholdSlider.value)}°"
    emergThresholdValue.text = "${"+%.1f".format(emergThresholdSlider.value)}°"
    minVoltageValue.text = "${"%.1f".format(minVoltageSlider.value)} В"

    val values = stepRangeSlider.values
    if (values[0] != values[1])
      stepRangeValue.text = "${values[0].toInt()} - ${values[1].toInt()} кОм"
    else
      stepRangeValue.text = "${values[0].toInt()} кОм"

    hysteresisValue.text = "${"%.1f".format(hysteresisSlider.value)}°"
    maxSpeedValue.text = "${maxSpeedSlider.value.toInt()} °/мс"
    trendDurationValue.text = "${trendDurationSlider.value.toInt()} мс"
    stableDurationValue.text = "${stableDurationSlider.value.toInt()} мс"
  }

  private fun loadSettings(settings: Settings) {
    systemTickSlider.value = settings.system_tick.toFloat()
    predictionHorizontSlider.valueFrom = settings.system_tick.toFloat()
    predictionHorizontSlider.value =
      max(settings.prediction_horizont.toFloat(), settings.system_tick.toFloat())
    gyroHysteresisSlider.value = settings.gyro_hysteresis;
    targetPitchSlider.value = settings.target_pitch
    deadZoneSlider.value = settings.dead_zone
    exitThresholdSlider.value = settings.exit_threshold
    emergThresholdSlider.value = settings.emerg_threshold
    minVoltageSlider.value = settings.min_voltage
    stepRangeSlider.values = listOf(settings.min_step.toFloat(), settings.max_step.toFloat())
    hysteresisSlider.value = settings.hysteresis
    maxSpeedSlider.value = settings.max_speed.toFloat()
    trendDurationSlider.value = settings.trend_duration.toFloat()
    stableDurationSlider.value = settings.stable_duration.toFloat()
    reversedPitchValue.isChecked = settings.reversed_pitch
    reversedRollValue.isChecked = settings.reversed_roll

    updateAllTextValues()
  }

  private fun saveSettings(): Boolean {
    val settings = Settings(
      system_tick = systemTickSlider.value.toInt(),
      prediction_horizont = predictionHorizontSlider.value.toInt(),
      gyro_hysteresis = gyroHysteresisSlider.value,
      target_pitch = targetPitchSlider.value,
      dead_zone = deadZoneSlider.value,
      exit_threshold = exitThresholdSlider.value,
      emerg_threshold = emergThresholdSlider.value,
      min_voltage = minVoltageSlider.value,
      min_step = stepRangeSlider.values[0].toInt(),
      max_step = stepRangeSlider.values[1].toInt(),
      hysteresis = hysteresisSlider.value,
      max_speed = maxSpeedSlider.value.toInt(),
      trend_duration = trendDurationSlider.value.toInt(),
      stable_duration = stableDurationSlider.value.toInt(),
      reversed_pitch = reversedPitchValue.isChecked,
      reversed_roll = reversedRollValue.isChecked
    )

    if (!SettingsManager.validate(settings)) {
      Toast.makeText(this, "Ошибка валидации настроек", Toast.LENGTH_SHORT).show()
      return false
    }

    SettingsManager.currentSettings = settings

    sendCallback?.invoke(SettingsManager.toJson(settings))

    return true
  }
}

