package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_angle_settings, container, false)

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

  override fun setSettings(settings: Settings) {
    targetPitchSlider.value = settings.target_pitch
    deadZoneSlider.value = settings.dead_zone
    exitThresholdSlider.value = settings.exit_threshold
    emergThresholdSlider.value = settings.emerg_threshold

    updateTextValues()
  }

  private fun updateTextValues() {
    targetPitchValue.text = "${targetPitchSlider.value.toInt()}°"
    deadZoneValue.text = "${"%.1f".format(deadZoneSlider.value)}°"
    exitThresholdValue.text = "${"-%.1f".format(exitThresholdSlider.value)}°"
    emergThresholdValue.text = "${"+%.1f".format(emergThresholdSlider.value)}°"
  }

  override fun getSettings(settings: Settings) {
    settings.target_pitch = targetPitchSlider.value
    settings.dead_zone = deadZoneSlider.value
    settings.exit_threshold = exitThresholdSlider.value
    settings.emerg_threshold = emergThresholdSlider.value
  }
}
