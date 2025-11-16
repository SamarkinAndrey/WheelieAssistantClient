package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

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

  override fun loadSettings(settings: Settings) {
    minVoltageSlider.value = settings.min_voltage
    stepRangeSlider.values = listOf(settings.min_step.toFloat(), settings.max_step.toFloat())
  }

  override fun updateTextValues() {
    minVoltageValue.text = "${"%.1f".format(minVoltageSlider.value)} В"

    val values = stepRangeSlider.values
    if (values[0] != values[1])
      stepRangeValue.text = "${values[0].toInt()} - ${values[1].toInt()} кОм"
    else
      stepRangeValue.text = "${values[0].toInt()} кОм"
  }

  override fun saveSettings(settings: Settings) {
    settings.min_voltage = minVoltageSlider.value
    settings.min_step = stepRangeSlider.values[0].toInt()
    settings.max_step = stepRangeSlider.values[1].toInt()
  }
}