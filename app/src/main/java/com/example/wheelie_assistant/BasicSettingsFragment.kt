package com.app.wheelie_assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import androidx.appcompat.widget.SwitchCompat
import kotlin.math.max

class BasicSettingsFragment : Fragment() {

  private lateinit var systemTickValue: MaterialTextView
  private lateinit var predictionHorizontValue: MaterialTextView
  private lateinit var gyroHysteresisValue: MaterialTextView
  private lateinit var reversedPitchValue: SwitchCompat
  private lateinit var reversedRollValue: SwitchCompat

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

  fun loadSettings(settings: Settings) {
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

  fun getSettings(): SettingsPart {
    return SettingsPart(
      system_tick = systemTickSlider.value.toInt(),
      prediction_horizont = predictionHorizontSlider.value.toInt(),
      gyro_hysteresis = gyroHysteresisSlider.value,
      reversed_pitch = reversedPitchValue.isChecked,
      reversed_roll = reversedRollValue.isChecked
    )
  }
}

data class SettingsPart(
  val system_tick: Int = 0,
  val prediction_horizont: Int = 0,
  val gyro_hysteresis: Float = 0f,
  val reversed_pitch: Boolean = false,
  val reversed_roll: Boolean = false,
  val target_pitch: Float = 0f,
  val dead_zone: Float = 0f,
  val exit_threshold: Float = 0f,
  val emerg_threshold: Float = 0f,
  val min_voltage: Float = 0f,
  val min_step: Int = 0,
  val max_step: Int = 0,
  val hysteresis: Float = 0f,
  val max_speed: Int = 0,
  val trend_duration: Int = 0,
  val stable_duration: Int = 0
)