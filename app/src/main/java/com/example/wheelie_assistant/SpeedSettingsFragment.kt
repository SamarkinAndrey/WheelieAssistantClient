package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class SpeedSettingsFragment : SettingsFragment() {
  private lateinit var hysteresisValue: MaterialTextView
  private lateinit var maxSpeedValue: MaterialTextView
  private lateinit var trendDurationValue: MaterialTextView
  private lateinit var stableDurationValue: MaterialTextView

  private lateinit var hysteresisSlider: Slider
  private lateinit var maxSpeedSlider: Slider
  private lateinit var trendDurationSlider: Slider
  private lateinit var stableDurationSlider: Slider

  override fun getFragmentID(): Int = R.layout.fragment_speed_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    hysteresisValue = view.findViewById(R.id.hysteresis_value)
    maxSpeedValue = view.findViewById(R.id.max_speed_value)
    trendDurationValue = view.findViewById(R.id.trend_duration_value)
    stableDurationValue = view.findViewById(R.id.stable_duration_value)

    hysteresisSlider = view.findViewById(R.id.hysteresis_slider)
    maxSpeedSlider = view.findViewById(R.id.max_speed_slider)
    trendDurationSlider = view.findViewById(R.id.trend_duration_slider)
    stableDurationSlider = view.findViewById(R.id.stable_duration_slider)
  }

  private fun setupSliders() {
    hysteresisSlider.addOnChangeListener { _, value, _ ->
      hysteresisValue.text = "${"%.1f".format(value)}°"
    }

    maxSpeedSlider.addOnChangeListener { _, value, _ ->
      maxSpeedValue.text = "${value.toInt()} °/мс"
    }

    trendDurationSlider.addOnChangeListener { _, value, _ ->
      trendDurationValue.text = "${value.toInt()} мс"
    }

    stableDurationSlider.addOnChangeListener { _, value, _ ->
      stableDurationValue.text = "${value.toInt()} мс"
    }
  }

  override fun onLoadSettings(settings: Settings) {
    hysteresisSlider.apply { value = settings.hysteresis.coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }
    maxSpeedSlider.apply { value = settings.max_speed.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }
    trendDurationSlider.apply { value = settings.trend_duration.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }
    stableDurationSlider.apply { value = settings.stable_duration.toFloat().coerceIn(minOf(valueFrom, valueTo), maxOf(valueFrom, valueTo)) }
  }

  override fun onUpdateTextValues() {
    hysteresisValue.text = "${"%.1f".format(hysteresisSlider.value)}°"
    maxSpeedValue.text = "${maxSpeedSlider.value.toInt()} °/мс"
    trendDurationValue.text = "${trendDurationSlider.value.toInt()} мс"
    stableDurationValue.text = "${stableDurationSlider.value.toInt()} мс"
  }

  override fun onSaveSettings(settings: Settings) {
    settings.hysteresis = hysteresisSlider.value
    settings.max_speed = maxSpeedSlider.value.toInt()
    settings.trend_duration = trendDurationSlider.value.toInt()
    settings.stable_duration = stableDurationSlider.value.toInt()
  }
}