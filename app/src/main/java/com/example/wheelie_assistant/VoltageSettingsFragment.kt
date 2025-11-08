package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class VoltageSettingsFragment : BaseSettingsFragment() {
  private lateinit var minVoltageValue: MaterialTextView
  private lateinit var stepRangeValue: MaterialTextView

  private lateinit var minVoltageSlider: Slider
  private lateinit var stepRangeSlider: RangeSlider

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_voltage_settings, container, false)

    initViews(view)
    setupSliders()
    setSettings(SettingsManager.currentSettings)
    val scrollLayout = view.findViewById<LinearLayout>(R.id.scroll_layout)
    val activity = SettingsActivity.getInstance()

    activity?.let {
      val saveLayout = it.getSaveLayout()

      saveLayout.post {
        scrollLayout.setPadding(
          scrollLayout.paddingStart,
          scrollLayout.paddingTop,
          scrollLayout.paddingEnd,
          saveLayout.height
        )
      }
    }

    return view
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

  override fun setSettings(settings: Settings) {
    minVoltageSlider.value = settings.min_voltage
    stepRangeSlider.values = listOf(settings.min_step.toFloat(), settings.max_step.toFloat())

    updateTextValues()
  }

  private fun updateTextValues() {
    minVoltageValue.text = "${"%.1f".format(minVoltageSlider.value)} В"

    val values = stepRangeSlider.values
    if (values[0] != values[1])
      stepRangeValue.text = "${values[0].toInt()} - ${values[1].toInt()} кОм"
    else
      stepRangeValue.text = "${values[0].toInt()} кОм"
  }

  override fun getSettings(settings: Settings) {
    settings.min_voltage = minVoltageSlider.value
    settings.min_step = stepRangeSlider.values[0].toInt()
    settings.max_step = stepRangeSlider.values[1].toInt()
  }
}