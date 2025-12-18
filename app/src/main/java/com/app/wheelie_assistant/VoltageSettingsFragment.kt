package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import kotlin.math.round

class VoltageSettingsFragment : SettingsFragment() {
  private lateinit var minVoltageValue: MaterialTextView
  private lateinit var stepRangeValue: MaterialTextView

  private lateinit var minVoltageSlider: Slider
  private lateinit var stepRangeSlider: RangeSlider

  override fun getFragmentID(): Int = R.layout.fragment_voltage_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    minVoltageValue = view.findViewById(R.id.min_voltage_value)
    stepRangeValue = view.findViewById(R.id.step_range_value)

    minVoltageSlider = view.findViewById(R.id.min_voltage_slider)
    stepRangeSlider = view.findViewById(R.id.step_range_slider)
  }

  private fun setupSliders() {
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
  }

  override fun onLoadSettings(settings: Settings) {
    minVoltageSlider.apply {
      if (settings.voltageRangeValid()) {
        valueFrom = round(settings.voltage_range_min * 10) / 10
        valueTo = round(settings.voltage_range_max * 10) / 10
      } else {
        valueFrom = Limits.VOLTAGE_RANGE_MIN
        valueTo = Limits.VOLTAGE_RANGE_MAX
      }
      value = settings.voltage_min.coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo))
    }
    stepRangeSlider.apply { values = listOf(settings.step_min.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)),
                                            settings.step_max.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo))) }
  }

  override fun onUpdateTextValues() {
    minVoltageValue.text = "${"%.1f".format(minVoltageSlider.value)} В"

    val values = stepRangeSlider.values
    if (values[0] != values[1])
      stepRangeValue.text = "${values[0].toInt()} - ${values[1].toInt()} кОм"
    else
      stepRangeValue.text = "${values[0].toInt()} кОм"
  }

  override fun onSaveSettings(settings: Settings) {
    settings.voltage_min = minVoltageSlider.value
    settings.step_min = stepRangeSlider.values[0].toInt()
    settings.step_max = stepRangeSlider.values[1].toInt()
  }
}