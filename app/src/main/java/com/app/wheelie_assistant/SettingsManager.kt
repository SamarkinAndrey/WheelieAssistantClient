package com.app.wheelie_assistant

import com.app.wheelie_assistant.BTParam.*

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
  var firmware_ver: String = "",
  var wifi_timeout: Int = 0
) {
  fun loadFrom(parser: JsonParamParser, checkParams: Boolean = true) {
    if (!checkParams || parser.hasParam(B_SYSTEM_TICK))
      system_tick = parser.getInt(B_SYSTEM_TICK)
    if (!checkParams || parser.hasParam(B_PREDICTION_HORIZONT))
      prediction_horizont = parser.getInt(B_PREDICTION_HORIZONT)
    if (!checkParams || parser.hasParam(B_GYRO_HYSTERESIS))
      gyro_hysteresis = parser.getFloat(B_GYRO_HYSTERESIS)
    if (!checkParams || parser.hasParam(B_REVERSED_PITCH))
      reversed_pitch = parser.getBoolean(B_REVERSED_PITCH)
    if (!checkParams || parser.hasParam(B_REVERSED_ROLL))
      reversed_roll = parser.getBoolean(B_REVERSED_ROLL)
    if (!checkParams || parser.hasParam(B_TARGET_PITCH))
      target_pitch = parser.getFloat(B_TARGET_PITCH)
    if (!checkParams || parser.hasParam(B_DEAD_ZONE))
      dead_zone = parser.getFloat(B_DEAD_ZONE)
    if (!checkParams || parser.hasParam(B_EXIT_THRESHOLD))
      exit_threshold = parser.getFloat(B_EXIT_THRESHOLD)
    if (!checkParams || parser.hasParam(B_EMERG_THRESHOLD))
      emerg_threshold = parser.getFloat(B_EMERG_THRESHOLD)
    if (!checkParams || parser.hasParam(B_MIN_VOLTAGE))
      min_voltage = parser.getFloat(B_MIN_VOLTAGE)
    if (!checkParams || parser.hasParam(B_MIN_STEP))
      min_step = parser.getInt(B_MIN_STEP)
    if (!checkParams || parser.hasParam(B_MAX_STEP))
      max_step = parser.getInt(B_MAX_STEP)
    if (!checkParams || parser.hasParam(B_HYSTERESIS))
      hysteresis = parser.getFloat(B_HYSTERESIS)
    if (!checkParams || parser.hasParam(B_MAX_SPEED))
      max_speed = parser.getInt(B_MAX_SPEED)
    if (!checkParams || parser.hasParam(B_TREND_DURATION))
      trend_duration = parser.getInt(B_TREND_DURATION)
    if (!checkParams || parser.hasParam(B_STABLE_DURATION))
      stable_duration = parser.getInt(B_STABLE_DURATION)
    if (!checkParams || parser.hasParam(B_FIRMWARE_VERSION))
      firmware_ver = parser.getString(B_FIRMWARE_VERSION)
    if (!checkParams || parser.hasParam(B_WIFI_TIMEOUT))
      wifi_timeout = parser.getInt(B_WIFI_TIMEOUT)
  }

  fun saveTo(parser: JsonParamParser) {
    parser.setInt(B_SYSTEM_TICK, system_tick)
    parser.setInt(B_PREDICTION_HORIZONT, prediction_horizont)
    parser.setFloat(B_GYRO_HYSTERESIS, gyro_hysteresis)
    parser.setBoolean(B_REVERSED_PITCH, reversed_pitch)
    parser.setBoolean(B_REVERSED_ROLL, reversed_roll)
    parser.setFloat(B_TARGET_PITCH, target_pitch)
    parser.setFloat(B_DEAD_ZONE, dead_zone)
    parser.setFloat(B_EXIT_THRESHOLD, exit_threshold)
    parser.setFloat(B_EMERG_THRESHOLD, emerg_threshold)
    parser.setFloat(B_MIN_VOLTAGE, min_voltage)
    parser.setInt(B_MIN_STEP, min_step)
    parser.setInt(B_MAX_STEP, max_step)
    parser.setFloat(B_HYSTERESIS, hysteresis)
    parser.setInt(B_MAX_SPEED, max_speed)
    parser.setInt(B_TREND_DURATION, trend_duration)
    parser.setInt(B_STABLE_DURATION, stable_duration)
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
    reversed_pitch = false
    reversed_roll = false
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
    firmware_ver = ""
    wifi_timeout = 0
  }

  fun default() {
    system_tick = 10
    prediction_horizont = 90
    gyro_hysteresis = 0.2f
    reversed_pitch = false
    reversed_roll = false
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
  }
}

object SettingsManager {
  var settings = Settings()
  private var loaded = false

  fun loadFrom(parser: JsonParamParser) {
    settings.loadFrom(parser)
    loaded = true
  }

  fun saveTo(parser: JsonParamParser) {
    settings.saveTo(parser)
  }

  fun validate(): Boolean {
    return settings.validate()
  }

  fun clear() {
    settings.clear()
    loaded = false
  }

  fun default() {
    settings.default()
    loaded = false
  }

//  fun settings(): Settings = settings

  fun isLoaded() = loaded
}