package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.math.max

class BasicSettingsFragment : BaseSettingsFragment() {

  private lateinit var systemTickValue: MaterialTextView
  private lateinit var predictionHorizontValue: MaterialTextView
  private lateinit var gyroHysteresisValue: MaterialTextView
  private lateinit var reversedPitchValue: SwitchMaterial
  private lateinit var reversedRollValue: SwitchMaterial

  private lateinit var systemTickSlider: Slider
  private lateinit var predictionHorizontSlider: Slider
  private lateinit var gyroHysteresisSlider: Slider

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_basic_settings, container, false)

    initViews(view)
    setupSliders()
    setSettings(SettingsManager.currentSettings)

    val scrollLayout = view.findViewById<LinearLayout>(R.id.scroll_layout)
    val activity = SettingsActivity.getInstance()

    activity?.let {
      val saveLayout = it.getSaveLayout()

      saveLayout.post {
        scrollLayout.setPadding(
          scrollLayout.paddingLeft,
          scrollLayout.paddingTop,
          scrollLayout.paddingRight,
          saveLayout.height
        )
      }
    }

    return view
  }

  private fun initViews(view: View) {
    systemTickValue = view.findViewById(R.id.system_tick_value)
    predictionHorizontValue = view.findViewById(R.id.prediction_horizont_value)
    gyroHysteresisValue = view.findViewById(R.id.gyro_hysteresis_value)
    reversedPitchValue = view.findViewById(R.id.reversed_pitch_value)
    reversedRollValue = view.findViewById(R.id.reversed_roll_value)

    systemTickSlider = view.findViewById(R.id.system_tick_slider)
    predictionHorizontSlider = view.findViewById(R.id.prediction_horizont_slider)
    gyroHysteresisSlider = view.findViewById(R.id.gyro_hysteresis_slider)
  }

  private fun setupSliders() {
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

  override fun setSettings(settings: Settings) {
    systemTickSlider.value = settings.system_tick.toFloat()
    predictionHorizontSlider.valueFrom = settings.system_tick.toFloat()
    predictionHorizontSlider.value = max(settings.prediction_horizont.toFloat(), settings.system_tick.toFloat())
    gyroHysteresisSlider.value = settings.gyro_hysteresis
    reversedPitchValue.isChecked = settings.reversed_pitch
    reversedRollValue.isChecked = settings.reversed_roll

    updateTextValues()
  }

  private fun updateTextValues() {
    systemTickValue.text = "${systemTickSlider.value.toInt()} мс"
    predictionHorizontValue.text = "${predictionHorizontSlider.value.toInt()} мс"
    gyroHysteresisValue.text = "${"%.1f".format(gyroHysteresisSlider.value)}°"
  }

  override fun getSettings(settings: Settings) {
    settings.system_tick = systemTickSlider.value.toInt()
    settings.prediction_horizont = predictionHorizontSlider.value.toInt()
    settings.gyro_hysteresis = gyroHysteresisSlider.value
    settings.reversed_pitch = reversedPitchValue.isChecked
    settings.reversed_roll = reversedRollValue.isChecked
  }
}
