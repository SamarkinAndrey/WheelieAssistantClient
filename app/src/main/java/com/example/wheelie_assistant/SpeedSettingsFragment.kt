package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class SpeedSettingsFragment : BaseSettingsFragment() {
  private lateinit var hysteresisValue: MaterialTextView
  private lateinit var maxSpeedValue: MaterialTextView
  private lateinit var trendDurationValue: MaterialTextView
  private lateinit var stableDurationValue: MaterialTextView

  private lateinit var hysteresisSlider: Slider
  private lateinit var maxSpeedSlider: Slider
  private lateinit var trendDurationSlider: Slider
  private lateinit var stableDurationSlider: Slider

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_speed_settings, container, false)

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

  override fun setSettings(settings: Settings) {
    hysteresisSlider.value = settings.hysteresis
    maxSpeedSlider.value = settings.max_speed.toFloat()
    trendDurationSlider.value = settings.trend_duration.toFloat()
    stableDurationSlider.value = settings.stable_duration.toFloat()

    updateTextValues()
  }

  private fun updateTextValues() {
    hysteresisValue.text = "${"%.1f".format(hysteresisSlider.value)}°"
    maxSpeedValue.text = "${maxSpeedSlider.value.toInt()} °/мс"
    trendDurationValue.text = "${trendDurationSlider.value.toInt()} мс"
    stableDurationValue.text = "${stableDurationSlider.value.toInt()} мс"
  }

  override fun getSettings(settings: Settings) {
    settings.hysteresis = hysteresisSlider.value
    settings.max_speed = maxSpeedSlider.value.toInt()
    settings.trend_duration = trendDurationSlider.value.toInt()
    settings.stable_duration = stableDurationSlider.value.toInt()
  }
}