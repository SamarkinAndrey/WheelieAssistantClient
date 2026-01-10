package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.RangeSlider

class BasicSettingsFragment : SettingsFragment() {

  private lateinit var chipFreqValue: MaterialTextView
  private lateinit var predictionHorizontValue: MaterialTextView
  private lateinit var speedRangeValue: MaterialTextView
  private lateinit var gyroHysteresisValue: MaterialTextView
  private lateinit var reversedPitchValue: MaterialSwitch
  private lateinit var reversedRollValue: MaterialSwitch

  private lateinit var chipFreqSlider: Slider
  private lateinit var predictionHorizontSlider: Slider
  private lateinit var speedRangeSlider: RangeSlider
  private lateinit var gyroHysteresisSlider: Slider

  override fun getFragmentID(): Int = R.layout.fragment_basic_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    chipFreqValue = view.findViewById(R.id.chip_freq_value)
    predictionHorizontValue = view.findViewById(R.id.prediction_horizon_value)
    speedRangeValue = view.findViewById(R.id.speed_range_value)
    gyroHysteresisValue = view.findViewById(R.id.gyro_hysteresis_value)
    reversedPitchValue = view.findViewById(R.id.reversed_pitch_value)
    reversedRollValue = view.findViewById(R.id.reversed_roll_value)

    chipFreqSlider = view.findViewById(R.id.chip_freq_slider)
    predictionHorizontSlider = view.findViewById(R.id.prediction_horizon_slider)
    speedRangeSlider = view.findViewById(R.id.speed_range_slider)
    gyroHysteresisSlider = view.findViewById(R.id.gyro_hysteresis_slider)
  }

  private fun setupSliders() {
    chipFreqSlider.addOnChangeListener { slider, value, fromUser ->
      chipFreqValue.text = "${value.toInt()} Mhz"
    }

    predictionHorizontSlider.addOnChangeListener { _, value, _ ->
      predictionHorizontValue.text = "${value.toInt()} мс"
    }

    speedRangeSlider.addOnChangeListener { _, value, fromUser ->
      val values = speedRangeSlider.values
      val strVal = { v: Float ->
        val intVal = v.toInt()
        if (intVal > 0) "+$intVal" else "$intVal"
      }
      if (values[0] != values[1])
        speedRangeValue.text = "${strVal(values[0])} .. ${strVal(values[1])}"
      else
        speedRangeValue.text = strVal(values[0])
    }

    gyroHysteresisSlider.addOnChangeListener { _, value, _ ->
      gyroHysteresisValue.text = "${"%.1f".format(value)}°"
    }
  }

  override fun onLoadSettings(settings: Settings) {
    chipFreqSlider.apply { value = settings.chip_freq.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }

    predictionHorizontSlider.apply {
      value = settings.prediction_horizon.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo))
    }

    speedRangeSlider.apply {
      values = listOf(settings.speed_min.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)),
                      settings.speed_max.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)))
    }

    gyroHysteresisSlider.apply { value = settings.gyro_hysteresis.coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }

    reversedPitchValue.isChecked = settings.reversed_pitch
    reversedRollValue.isChecked = settings.reversed_roll
  }

  override fun onUpdateTextValues() {
    chipFreqValue.text = "${chipFreqSlider.value.toInt()} Mhz"
    predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"

    val values = speedRangeSlider.values
    val strVal = { v: Float ->
      val intVal = v.toInt()
      if (intVal > 0) "+$intVal" else "$intVal"
    }
    if (values[0] != values[1])
      speedRangeValue.text = "${strVal(values[0])} .. ${strVal(values[1])}"
    else
      speedRangeValue.text = strVal(values[0])

    gyroHysteresisValue.text = "${"%.1f".format(gyroHysteresisSlider.value)}°"
  }

  override fun onSaveSettings(settings: Settings) {
    settings.chip_freq = chipFreqSlider.value.toInt()
    settings.prediction_horizon = predictionHorizontSlider.value.toInt()
    settings.speed_min = speedRangeSlider.values[0].toInt()
    settings.speed_max = speedRangeSlider.values[1].toInt()
    settings.gyro_hysteresis = gyroHysteresisSlider.value
    settings.reversed_pitch = reversedPitchValue.isChecked
    settings.reversed_roll = reversedRollValue.isChecked
  }
}
