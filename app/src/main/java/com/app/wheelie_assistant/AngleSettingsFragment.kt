package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class AngleSettingsFragment : SettingsFragment() {
  private lateinit var targetPitchValue: MaterialTextView
  private lateinit var enterPitchValue: MaterialTextView
  private lateinit var emergPitchValue: MaterialTextView
  private lateinit var deadZoneValue: MaterialTextView

  private lateinit var targetPitchSlider: Slider
  private lateinit var enterPitchSlider: Slider
  private lateinit var emergPitchSlider: Slider
  private lateinit var deadZoneSlider: Slider

  private var enterThreshold: Float = 0.0f
  private var emergThreshold: Float = 0.0f

  override fun getFragmentID(): Int = R.layout.fragment_angle_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    targetPitchValue = view.findViewById(R.id.target_pitch_value)
    deadZoneValue = view.findViewById(R.id.dead_zone_value)
    enterPitchValue = view.findViewById(R.id.enter_pitch_value)
    emergPitchValue = view.findViewById(R.id.emerg_pitch_value)

    targetPitchSlider = view.findViewById(R.id.target_pitch_slider)
    deadZoneSlider = view.findViewById(R.id.dead_zone_slider)
    enterPitchSlider = view.findViewById(R.id.enter_pitch_slider)
    emergPitchSlider = view.findViewById(R.id.emerg_pitch_slider)
  }

  private fun setupSliders() {
    targetPitchSlider.addOnChangeListener { _, value, _ ->
      targetPitchValue.text = "${value.toInt()}°"

      calcThresholds()
    }

    enterPitchSlider.addOnChangeListener { _, value, _ ->
      enterThreshold = targetPitchSlider.value - value
      enterPitchValue.text = "${value.toInt()}°"
    }

    emergPitchSlider.addOnChangeListener { _, value, _ ->
      emergThreshold = value - targetPitchSlider.value
      emergPitchValue.text = "${value.toInt()}°"
    }

    deadZoneSlider.addOnChangeListener { _, value, _ ->
      deadZoneValue.text = "${"%.1f".format(value)}°"
    }
  }

  override fun onLoadSettings(settings: Settings) {
    targetPitchSlider.apply { value = settings.target_pitch.coerceIn(valueFrom, valueTo) }
    deadZoneSlider.apply { value = settings.dead_zone.coerceIn(valueFrom, valueTo) }

    enterThreshold = settings.enter_threshold
    emergThreshold = settings.emerg_threshold

    calcThresholds()
  }

  override fun onUpdateTextValues() {
    targetPitchValue.text = "${targetPitchSlider.value.toInt()}°"
    deadZoneValue.text = "${"%.1f".format(deadZoneSlider.value)}°"
    enterPitchValue.text = "${enterPitchSlider.value.toInt()}°"
    emergPitchValue.text = "${emergPitchSlider.value.toInt()}°"
  }

  override fun onSaveSettings(settings: Settings) {
    settings.target_pitch = targetPitchSlider.value
    settings.dead_zone = deadZoneSlider.value
    settings.enter_threshold = enterThreshold
    settings.emerg_threshold = emergThreshold
  }

  private fun calcThresholds() {
    enterPitchSlider.apply {
      valueFrom = targetPitchSlider.value - Limits.ENTER_THRESHOLD_MAX
      valueTo = targetPitchSlider.value
      value = valueTo - enterThreshold
    }
    emergPitchSlider.apply {
      valueFrom = targetPitchSlider.value
      valueTo = targetPitchSlider.value + Limits.EMERG_THRESHOLD_MAX
      value = valueFrom + emergThreshold
    }
  }
}
