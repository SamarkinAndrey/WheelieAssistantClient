package com.app.wheelie_assistant

import com.app.wheelie_assistant.BTParam.*

data class Settings(
  var chip_freq: Int = Defaults.CHIP_FREQ,
  var prediction_horizon: Int = Defaults.PREDICTION_HORIZON,
  var speed_min: Int = Defaults.SPEED_MIN,
  var speed_max: Int = Defaults.SPEED_MAX,
  var gyro_hysteresis: Float = Defaults.GYRO_HYSTERESIS,
  var target_pitch: Float = Defaults.TARGET_PITCH,
  var dead_zone: Float = Defaults.DEAD_ZONE,
  var enter_threshold: Float = Defaults.ENTER_THRESHOLD,
  var emerg_threshold: Float = Defaults.EMERG_THRESHOLD,
  var voltage_range_min: Float = Defaults.VOLTAGE_RANGE_MIN,
  var voltage_range_max: Float = Defaults.VOLTAGE_RANGE_MAX,
  var hysteresis: Float = Defaults.HYSTERESIS,
  var max_speed: Int = Defaults.MAX_SPEED,
  var trend_duration: Int = Defaults.TREND_DURATION,
  var stable_duration: Int = Defaults.STABLE_DURATION,
  var reversed_pitch: Boolean = Defaults.REVERSED_PITCH,
  var reversed_roll: Boolean = Defaults.REVERSED_ROLL,
  var firmware_ver: String = "",
  var wifi_timeout: Int = 0
) {
  fun loadFrom(parser: JsonParamParser, checkParams: Boolean = true) {
    if (!checkParams || parser.hasParam(B_CHIP_FREQ))
      chip_freq = parser.getInt(B_CHIP_FREQ)
    if (!checkParams || parser.hasParam(B_PREDICTION_HORIZON))
      prediction_horizon = parser.getInt(B_PREDICTION_HORIZON)
    if (!checkParams || parser.hasParam(B_SPEED_MIN))
      speed_min = parser.getInt(B_SPEED_MIN)
    if (!checkParams || parser.hasParam(B_SPEED_MAX))
      speed_max = parser.getInt(B_SPEED_MAX)
    if (!checkParams || parser.hasParam(B_GYRO_HYSTERESIS))
      gyro_hysteresis = parser.getFloat(B_GYRO_HYSTERESIS)
    if (!checkParams || parser.hasParam(B_TARGET_PITCH))
      target_pitch = parser.getFloat(B_TARGET_PITCH)
    if (!checkParams || parser.hasParam(B_DEAD_ZONE))
      dead_zone = parser.getFloat(B_DEAD_ZONE)
    if (!checkParams || parser.hasParam(B_ENTER_THRESHOLD))
      enter_threshold = parser.getFloat(B_ENTER_THRESHOLD)
    if (!checkParams || parser.hasParam(B_EMERG_THRESHOLD))
      emerg_threshold = parser.getFloat(B_EMERG_THRESHOLD)
    if (!checkParams || parser.hasParam(B_VOLTAGE_RANGE_MIN))
      voltage_range_min = parser.getFloat(B_VOLTAGE_RANGE_MIN)
    if (!checkParams || parser.hasParam(B_VOLTAGE_RANGE_MAX))
      voltage_range_max = parser.getFloat(B_VOLTAGE_RANGE_MAX)
    if (!checkParams || parser.hasParam(B_HYSTERESIS))
      hysteresis = parser.getFloat(B_HYSTERESIS)
    if (!checkParams || parser.hasParam(B_MAX_SPEED))
      max_speed = parser.getInt(B_MAX_SPEED)
    if (!checkParams || parser.hasParam(B_TREND_DURATION))
      trend_duration = parser.getInt(B_TREND_DURATION)
    if (!checkParams || parser.hasParam(B_STABLE_DURATION))
      stable_duration = parser.getInt(B_STABLE_DURATION)
    if (!checkParams || parser.hasParam(B_REVERSED_PITCH))
      reversed_pitch = parser.getBoolean(B_REVERSED_PITCH)
    if (!checkParams || parser.hasParam(B_REVERSED_ROLL))
      reversed_roll = parser.getBoolean(B_REVERSED_ROLL)
    if (!checkParams || parser.hasParam(B_FIRMWARE_VERSION))
      firmware_ver = parser.getString(B_FIRMWARE_VERSION)
    if (!checkParams || parser.hasParam(B_WIFI_TIMEOUT))
      wifi_timeout = parser.getInt(B_WIFI_TIMEOUT)
  }

  fun saveTo(parser: JsonParamParser) {
    parser.setInt(B_CHIP_FREQ, chip_freq)
    parser.setInt(B_PREDICTION_HORIZON, prediction_horizon)
    parser.setInt(B_SPEED_MIN, speed_min)
    parser.setInt(B_SPEED_MAX, speed_max)
    parser.setFloat(B_GYRO_HYSTERESIS, gyro_hysteresis)
    parser.setFloat(B_TARGET_PITCH, target_pitch)
    parser.setFloat(B_DEAD_ZONE, dead_zone)
    parser.setFloat(B_ENTER_THRESHOLD, enter_threshold)
    parser.setFloat(B_EMERG_THRESHOLD, emerg_threshold)
    parser.setFloat(B_HYSTERESIS, hysteresis)
    parser.setInt(B_MAX_SPEED, max_speed)
    parser.setInt(B_TREND_DURATION, trend_duration)
    parser.setInt(B_STABLE_DURATION, stable_duration)
    parser.setBoolean(B_REVERSED_PITCH, reversed_pitch)
    parser.setBoolean(B_REVERSED_ROLL, reversed_roll)
  }

  fun validate(): Boolean {
    return try {
      (chip_freq in Limits.CHIP_FREQ_MIN..Limits.CHIP_FREQ_MAX) &&
      (prediction_horizon in Limits.PREDICTION_HORIZON_MIN..Limits.PREDICTION_HORIZON_MAX) &&
      (speed_min in Limits.SPEED_MIN..Limits.SPEED_MAX) &&
      (speed_max in speed_min..Limits.SPEED_MAX) &&
      (gyro_hysteresis in Limits.HYSTERESIS_MIN..Limits.HYSTERESIS_MAX) &&
      (target_pitch in Limits.TARGET_PITCH_MIN..Limits.TARGET_PITCH_MAX) &&
      (dead_zone in Limits.DEAD_ZONE_MIN..Limits.DEAD_ZONE_MAX) &&
      (enter_threshold in Limits.ENTER_THRESHOLD_MIN..Limits.ENTER_THRESHOLD_MAX) &&
      (emerg_threshold in Limits.EMERG_THRESHOLD_MIN..Limits.EMERG_THRESHOLD_MAX) &&
      (hysteresis in Limits.HYSTERESIS_MIN..Limits.HYSTERESIS_MAX) &&
      (max_speed in Limits.MAX_SPEED_MIN..Limits.MAX_SPEED_MAX) &&
      (trend_duration in Limits.TREND_DURATION_MIN..Limits.TREND_DURATION_MAX) &&
      (stable_duration in Limits.STABLE_DURATION_MIN..Limits.STABLE_DURATION_MAX)
    } catch (e: Exception) {
      false
    }
  }

  fun clear() {
    chip_freq = 0
    prediction_horizon = 0
    speed_min = 0
    speed_max = 0
    gyro_hysteresis = 0f
    target_pitch = 0f
    dead_zone = 0f
    enter_threshold = 0f
    emerg_threshold = 0f
    voltage_range_min = 0f
    voltage_range_max = 0f
    hysteresis = 0f
    max_speed = 0
    trend_duration = 0
    stable_duration = 0
    reversed_pitch = false
    reversed_roll = false
    firmware_ver = ""
    wifi_timeout = 0
  }

  fun default() {
    chip_freq = Defaults.CHIP_FREQ
    prediction_horizon = Defaults.PREDICTION_HORIZON
    speed_min = Defaults.SPEED_MIN
    speed_max = Defaults.SPEED_MAX
    gyro_hysteresis = Defaults.GYRO_HYSTERESIS
    target_pitch = Defaults.TARGET_PITCH
    dead_zone = Defaults.DEAD_ZONE
    enter_threshold = Defaults.ENTER_THRESHOLD
    emerg_threshold = Defaults.EMERG_THRESHOLD
    voltage_range_min = Defaults.VOLTAGE_RANGE_MIN
    voltage_range_max = Defaults.VOLTAGE_RANGE_MAX
    hysteresis = Defaults.HYSTERESIS
    max_speed = Defaults.MAX_SPEED
    trend_duration = Defaults.TREND_DURATION
    stable_duration = Defaults.STABLE_DURATION
    reversed_pitch = Defaults.REVERSED_PITCH
    reversed_roll = Defaults.REVERSED_ROLL
  }

  fun voltageRangeValid(): Boolean {
    return voltage_range_min != Defaults.VOLTAGE_RANGE_MIN &&
           voltage_range_max != Defaults.VOLTAGE_RANGE_MAX &&
           voltage_range_min >= 0 && voltage_range_max > voltage_range_min
  }

  fun voltageRangeClear() {
    voltage_range_min = Defaults.VOLTAGE_RANGE_MIN
    voltage_range_max = Defaults.VOLTAGE_RANGE_MAX
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

  fun voltageRangeValid(): Boolean {
    return settings.voltageRangeValid()
  }

  fun voltageRangeClear() {
    settings.voltageRangeClear()
  }

  //  fun settings(): Settings = settings

  fun isLoaded() = loaded
}