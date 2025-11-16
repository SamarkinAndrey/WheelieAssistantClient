package com.app.wheelie_assistant

import JsonParamParser
import BTParam.*

data class Settings(
  var system_tick: Int = 0,
  var prediction_horizont: Int = 0,
  var gyro_hysteresis: Float = 0f,
  var reversed_pitch: Boolean = false,
  var reversed_roll: Boolean = false,
  var target_pitch: Float = 0f,
  var dead_zone: Float = 0f,
  var exit_threshold: Float = 0f,
  var emerg_threshold: Float = 0f,
  var min_voltage: Float = 0f,
  var min_step: Int = 0,
  var max_step: Int = 0,
  var hysteresis: Float = 0f,
  var max_speed: Int = 0,
  var trend_duration: Int = 0,
  var stable_duration: Int = 0,
  var firmware_ver: String = ""
) {
  fun loadFrom(parser: JsonParamParser) {
    if (parser.hasParam(B_SYSTEM_TICK))
      system_tick = parser.getInt(B_SYSTEM_TICK, 0)
    if (parser.hasParam(B_PREDICTION_HORIZONT))
      prediction_horizont = parser.getInt(B_PREDICTION_HORIZONT, 0)
    if (parser.hasParam(B_GYRO_HYSTERESIS))
      gyro_hysteresis = parser.getFloat(B_GYRO_HYSTERESIS, 0f)
    if (parser.hasParam(B_REVERSED_PITCH))
      reversed_pitch = parser.getBoolean(B_REVERSED_PITCH, false)
    if (parser.hasParam(B_REVERSED_ROLL))
      reversed_roll = parser.getBoolean(B_REVERSED_ROLL, false)
    if (parser.hasParam(B_TARGET_PITCH))
      target_pitch = parser.getFloat(B_TARGET_PITCH, 0f)
    if (parser.hasParam(B_DEAD_ZONE))
      dead_zone = parser.getFloat(B_DEAD_ZONE, 0f)
    if (parser.hasParam(B_EXIT_THRESHOLD))
      exit_threshold = parser.getFloat(B_EXIT_THRESHOLD, 0f)
    if (parser.hasParam(B_EMERG_THRESHOLD))
      emerg_threshold = parser.getFloat(B_EMERG_THRESHOLD, 0f)
    if (parser.hasParam(B_MIN_VOLTAGE))
      min_voltage = parser.getFloat(B_MIN_VOLTAGE, 0f)
    if (parser.hasParam(B_MIN_STEP))
      min_step = parser.getInt(B_MIN_STEP, 0)
    if (parser.hasParam(B_MAX_STEP))
      max_step = parser.getInt(B_MAX_STEP, 0)
    if (parser.hasParam(B_HYSTERESIS))
      hysteresis = parser.getFloat(B_HYSTERESIS, 0f)
    if (parser.hasParam(B_MAX_SPEED))
      max_speed = parser.getInt(B_MAX_SPEED, 0)
    if (parser.hasParam(B_TREND_DURATION))
      trend_duration = parser.getInt(B_TREND_DURATION, 0)
    if (parser.hasParam(B_STABLE_DURATION))
      stable_duration = parser.getInt(B_STABLE_DURATION, 0)
    if (parser.hasParam(B_FIRMWARE_VERSION))
      firmware_ver = parser.getString(B_FIRMWARE_VERSION, "")
  }

  fun saveTo(parser: JsonParamParser) {
    parser.getInt(B_SYSTEM_TICK, system_tick)
    parser.getInt(B_PREDICTION_HORIZONT, prediction_horizont)
    parser.getFloat(B_GYRO_HYSTERESIS, gyro_hysteresis)
    parser.getBoolean(B_REVERSED_PITCH, reversed_pitch)
    parser.getBoolean(B_REVERSED_ROLL, reversed_roll)
    parser.getFloat(B_TARGET_PITCH, target_pitch)
    parser.getFloat(B_DEAD_ZONE, dead_zone)
    parser.getFloat(B_EXIT_THRESHOLD, exit_threshold)
    parser.getFloat(B_EMERG_THRESHOLD, emerg_threshold)
    parser.getFloat(B_MIN_VOLTAGE, min_voltage)
    parser.getInt(B_MIN_STEP, min_step)
    parser.getInt(B_MAX_STEP, max_step)
    parser.getFloat(B_HYSTERESIS, hysteresis)
    parser.getInt(B_MAX_SPEED, max_speed)
    parser.getInt(B_TREND_DURATION, trend_duration)
    parser.getInt(B_STABLE_DURATION, stable_duration)
  }

  fun validate(): Boolean {
    return try {
      (system_tick in 5..100) &&
        (prediction_horizont in system_tick..250) &&
        (gyro_hysteresis in 0.0f..5.0f) &&
        (reversed_pitch in false..true) &&
        (reversed_roll in false..true) &&
        (target_pitch in 15.0f..65.0f) &&
        (dead_zone in 0.0f..5.0f) &&
        (exit_threshold in 0.0f..10.0f) &&
        (emerg_threshold in 0.0f..10.0f) &&
        (min_voltage in 0.0f..5.0f) &&
        (min_step in 1..10) &&
        (max_step in min_step..10) &&
        (hysteresis in 0.0f..1.0f) &&
        (max_speed in 0..500) &&
        (trend_duration in 0..500) &&
        (stable_duration in 0..500)
    } catch (e: Exception) {
      false
    }
  }

  fun clear() {
    system_tick = 0
    prediction_horizont = 0
    gyro_hysteresis = 0f
    target_pitch = 0f
    dead_zone = 0f
    exit_threshold = 0f
    emerg_threshold = 0f
    min_voltage = 0f
    min_step = 0
    max_step = 0
    hysteresis = 0f
    max_speed = 0
    trend_duration = 0
    stable_duration = 0
    reversed_pitch = false
    reversed_roll = false
    firmware_ver = ""
  }

  fun default() {
    system_tick = 10
    prediction_horizont = 90
    gyro_hysteresis = 0.2f
    target_pitch = 25.0f
    dead_zone = 0.5f
    exit_threshold = 5.0f
    emerg_threshold = 5.0f
    min_voltage = 1.5f
    min_step = 1
    max_step = 4
    hysteresis = 0.3f
    max_speed = 200
    trend_duration = 50
    stable_duration = 300
    reversed_pitch = false
    reversed_roll = false
  }
}

object SettingsManager {
  var currentSettings = Settings()

  fun loadFrom(parser: JsonParamParser) {
    currentSettings.loadFrom(parser)
  }

  fun saveTo(parser: JsonParamParser) {
    currentSettings.saveTo(parser)
  }

  fun validate(): Boolean {
    return currentSettings.validate()
  }
//
//  fun saveSettings(): Boolean {
//    App.mainActivity?.bleManager?.let {
//      val parser = JsonParamParser()
//      if (saveTo(parser)) {
//        it.send(parser)
//        return true
//      }
//    }
//    return false
//  }
//
//  fun loadSettings(): Boolean {
//    App.mainActivity?.bleManager?.let {
//      it.send("${B_GET_SETTINGS}=1")
//      return true
//    }
//    return false
//  }
}