package com.app.wheelie_assistant

import com.app.wheelie_assistant.BTParam.*

data class Settings(
  var system_tick: Int = Defaults.SYSTEM_TICK,
  var prediction_horizont: Int = Defaults.PREDICT_HORIZONT,
  var gyro_hysteresis: Float = Defaults.GYRO_HYSTERESIS,
  var reversed_pitch: Boolean = false,
  var reversed_roll: Boolean = false,
  var target_pitch: Float = Defaults.TARGET_PITCH,
  var dead_zone: Float = Defaults.DEAD_ZONE,
  var exit_threshold: Float = Defaults.EXIT_THRESHOLD,
  var emerg_threshold: Float = Defaults.EMERG_THRESHOLD,
  var min_voltage: Float = Defaults.MIN_VOLTAGE,
  var chip_freq: Int = Defaults.CHIP_FREQ,
  var min_step: Int = Defaults.MIN_STEP,
  var max_step: Int = Defaults.MAX_STEP,
  var hysteresis: Float = Defaults.HYSTERESIS,
  var max_speed: Int = Defaults.MAX_SPEED,
  var trend_duration: Int = Defaults.TREND_DURATION,
  var stable_duration: Int = Defaults.STABLE_DURATION,
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
    if (!checkParams || parser.hasParam(B_CHIP_FREQ))
      chip_freq = parser.getInt(B_CHIP_FREQ)
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
    parser.setInt(B_CHIP_FREQ, chip_freq)
    parser.setInt(B_MIN_STEP, min_step)
    parser.setInt(B_MAX_STEP, max_step)
    parser.setFloat(B_HYSTERESIS, hysteresis)
    parser.setInt(B_MAX_SPEED, max_speed)
    parser.setInt(B_TREND_DURATION, trend_duration)
    parser.setInt(B_STABLE_DURATION, stable_duration)
  }

  fun validate(): Boolean {
    return try {
      (system_tick in Limits.SYSTEM_TICK_MIN..Limits.SYSTEM_TICK_MAX) &&
        (prediction_horizont in Limits.PREDICT_HORIZONT_MIN..Limits.PREDICT_HORIZONT_MAX) &&
        (gyro_hysteresis in Limits.GYRO_HYSTERESIS_MIN..Limits.GYRO_HYSTERESIS_MAX) &&
        (target_pitch in Limits.TARGET_PITCH_MIN..Limits.TARGET_PITCH_MAX) &&
        (dead_zone in Limits.DEAD_ZONE_MIN..Limits.DEAD_ZONE_MAX) &&
        (exit_threshold in Limits.EXIT_THRESHOLD_MIN..Limits.EXIT_THRESHOLD_MAX) &&
        (emerg_threshold in Limits.EMERG_THRESHOLD_MIN..Limits.EMERG_THRESHOLD_MAX) &&
        (min_voltage in Limits.MIN_VOLTAGE_MIN..Limits.MIN_VOLTAGE_MAX) &&
        (chip_freq in Limits.CHIP_FREQ_MIN..Limits.CHIP_FREQ_MAX) &&
        (min_step in Limits.STEP_MIN..Limits.STEP_MAX) &&
        (max_step in min_step..Limits.STEP_MAX) &&
        (hysteresis in Limits.HYSTERESIS_MIN..Limits.HYSTERESIS_MAX) &&
        (max_speed in Limits.MAX_SPEED_MIN..Limits.MAX_SPEED_MAX) &&
        (trend_duration in Limits.TREND_DURATION_MIN..Limits.TREND_DURATION_MAX) &&
        (stable_duration in Limits.STABLE_DURATION_MIN..Limits.STABLE_DURATION_MAX)
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
    chip_freq = 0
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
    system_tick = Defaults.SYSTEM_TICK
    prediction_horizont = Defaults.PREDICT_HORIZONT
    gyro_hysteresis = Defaults.GYRO_HYSTERESIS
    reversed_pitch = false
    reversed_roll = false
    target_pitch = Defaults.TARGET_PITCH
    dead_zone = Defaults.DEAD_ZONE
    exit_threshold = Defaults.EXIT_THRESHOLD
    emerg_threshold = Defaults.EMERG_THRESHOLD
    min_voltage = Defaults.MIN_VOLTAGE
    chip_freq = Defaults.CHIP_FREQ
    min_step = Defaults.MIN_STEP
    max_step = Defaults.MAX_STEP
    hysteresis = Defaults.HYSTERESIS
    max_speed = Defaults.MAX_SPEED
    trend_duration = Defaults.TREND_DURATION
    stable_duration = Defaults.STABLE_DURATION
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