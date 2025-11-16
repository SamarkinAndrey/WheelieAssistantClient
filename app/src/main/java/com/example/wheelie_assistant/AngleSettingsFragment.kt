package com.app.wheelie_assistant

import android.view.View
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

class AngleSettingsFragment : BaseSettingsFragment() {
  private lateinit var targetPitchValue: MaterialTextView
  private lateinit var deadZoneValue: MaterialTextView
  private lateinit var exitThresholdValue: MaterialTextView
  private lateinit var emergThresholdValue: MaterialTextView

  private lateinit var targetPitchSlider: Slider
  private lateinit var deadZoneSlider: Slider
  private lateinit var exitThresholdSlider: Slider
  private lateinit var emergThresholdSlider: Slider

  override fun getFragmentID(): Int = R.layout.fragment_angle_settings

  override fun onInit(view: View) {
    initViews(view)
    setupSliders()
  }

  private fun initViews(view: View) {
    targetPitchValue = view.findViewById(R.id.target_pitch_value)
    deadZoneValue = view.findViewById(R.id.dead_zone_value)
    exitThresholdValue = view.findViewById(R.id.exit_threshold_value)
    emergThresholdValue = view.findViewById(R.id.emerg_threshold_value)

    targetPitchSlider = view.findViewById(R.id.target_pitch_slider)
    deadZoneSlider = view.findViewById(R.id.dead_zone_slider)
    exitThresholdSlider = view.findViewById(R.id.exit_threshold_slider)
    emergThresholdSlider = view.findViewById(R.id.emerg_threshold_slider)
  }

  private fun setupSliders() {
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
  }

  override fun loadSettings(settings: Settings) {
    targetPitchSlider.value = settings.target_pitch
    deadZoneSlider.value = settings.dead_zone
    exitThresholdSlider.value = settings.exit_threshold
    emergThresholdSlider.value = settings.emerg_threshold
  }

  override fun updateTextValues() {
    targetPitchValue.text = "${targetPitchSlider.value.toInt()}°"
    deadZoneValue.text = "${"%.1f".format(deadZoneSlider.value)}°"
    exitThresholdValue.text = "${"-%.1f".format(exitThresholdSlider.value)}°"
    emergThresholdValue.text = "${"+%.1f".format(emergThresholdSlider.value)}°"
  }

  override fun saveSettings(settings: Settings) {
    settings.target_pitch = targetPitchSlider.value
    settings.dead_zone = deadZoneSlider.value
    settings.exit_threshold = exitThresholdSlider.value
    settings.emerg_threshold = emergThresholdSlider.value
  }
}
