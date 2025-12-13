package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.materialswitch.MaterialSwitch

class BasicSettingsFragment : SettingsFragment() {

  private lateinit var chipFreqValue: MaterialTextView
  private lateinit var systemTickValue: MaterialTextView
  private lateinit var predictionHorizontValue: MaterialTextView
  private lateinit var gyroHysteresisValue: MaterialTextView
  private lateinit var reversedPitchValue: MaterialSwitch
  private lateinit var reversedRollValue: MaterialSwitch

  private lateinit var chipFreqSlider: Slider
  private lateinit var systemTickSlider: Slider
  private lateinit var predictionHorizontSlider: Slider
  private lateinit var gyroHysteresisSlider: Slider

  override fun getFragmentID(): Int = R.layout.fragment_basic_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    chipFreqValue = view.findViewById(R.id.chip_freq_value)
    systemTickValue = view.findViewById(R.id.system_tick_value)
    predictionHorizontValue = view.findViewById(R.id.prediction_horizont_value)
    gyroHysteresisValue = view.findViewById(R.id.gyro_hysteresis_value)
    reversedPitchValue = view.findViewById(R.id.reversed_pitch_value)
    reversedRollValue = view.findViewById(R.id.reversed_roll_value)

    chipFreqSlider = view.findViewById(R.id.chip_freq_slider)
    systemTickSlider = view.findViewById(R.id.system_tick_slider)
    predictionHorizontSlider = view.findViewById(R.id.prediction_horizont_slider)
    gyroHysteresisSlider = view.findViewById(R.id.gyro_hysteresis_slider)
  }

  private fun setupSliders() {
    chipFreqSlider.addOnChangeListener { slider, value, fromUser ->
      chipFreqValue.text = "${value.toInt()} Mhz"
    }

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
  }

  private fun updatePredictionHorizont() {
    val valueFrom = systemTickSlider.value
    if (predictionHorizontSlider.value < valueFrom) {
      predictionHorizontSlider.value = valueFrom
      predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"
    }
    predictionHorizontSlider.valueFrom = valueFrom
  }

  override fun onLoadSettings(settings: Settings) {
    chipFreqSlider.apply { value = settings.chip_freq.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }

    systemTickSlider.apply { value = settings.system_tick.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }

    predictionHorizontSlider.apply {
      valueFrom = settings.system_tick.toFloat()
      value = settings.prediction_horizont.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo))
    }

    gyroHysteresisSlider.apply { value = settings.gyro_hysteresis.coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }

    reversedPitchValue.isChecked = settings.reversed_pitch
    reversedRollValue.isChecked = settings.reversed_roll
  }

  override fun onUpdateTextValues() {
    chipFreqValue.text = "${chipFreqSlider.value.toInt()} Mhz"
    systemTickValue.text = "${systemTickSlider.value.toInt()} мс"
    predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"
    gyroHysteresisValue.text = "${"%.1f".format(gyroHysteresisSlider.value)}°"
  }

  override fun onSaveSettings(settings: Settings) {
    settings.chip_freq = chipFreqSlider.value.toInt()
    settings.system_tick = systemTickSlider.value.toInt()
    settings.prediction_horizont = predictionHorizontSlider.value.toInt()
    settings.gyro_hysteresis = gyroHysteresisSlider.value
    settings.reversed_pitch = reversedPitchValue.isChecked
    settings.reversed_roll = reversedRollValue.isChecked
  }
}
