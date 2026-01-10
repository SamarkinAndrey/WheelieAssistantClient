package com.app.wheelie_assistant

import kotlin.String

object Defaults {
  const val CHIP_FREQ: Int = 80
  const val PREDICTION_HORIZON: Int = 90

  const val SPEED_MIN: Int = -5
  const val SPEED_MAX: Int = 5

  const val GYRO_HYSTERESIS: Float = 0.2f

  const val TARGET_PITCH: Float = 30.0f
  const val DEAD_ZONE: Float = 0.5f
  const val ENTER_THRESHOLD: Float = 10.0f
  const val EMERG_THRESHOLD: Float = 10.0f

  const val VOLTAGE_RANGE_MIN: Float = Float.POSITIVE_INFINITY
  const val VOLTAGE_RANGE_MAX: Float = Float.NEGATIVE_INFINITY

  const val HYSTERESIS: Float = 0.3f
  const val MAX_SPEED: Int = 200
  const val TREND_DURATION: Int = 50
  const val STABLE_DURATION: Int = 300

  const val REVERSED_PITCH: Boolean = false
  const val REVERSED_ROLL: Boolean = false
}

object Limits {
  const val CHIP_FREQ_MIN: Int = 80
  const val CHIP_FREQ_MAX: Int = 240

  const val PREDICTION_HORIZON_MIN: Int = 10
  const val PREDICTION_HORIZON_MAX: Int = 250

  const val SPEED_MIN: Int = -10
  const val SPEED_MAX: Int = 10

  const val TARGET_PITCH_MIN: Float = 15.0f
  const val TARGET_PITCH_MAX: Float = 65.0f

  const val DEAD_ZONE_MIN: Float = 0.0f
  const val DEAD_ZONE_MAX: Float = 5.0f

  const val ENTER_THRESHOLD_MIN: Float = 0.0f
  const val ENTER_THRESHOLD_MAX: Float = 10.0f

  const val EMERG_THRESHOLD_MIN: Float = 0.0f
  const val EMERG_THRESHOLD_MAX: Float = 10.0f

  const val VOLTAGE_RANGE_MIN: Float = 0.0f
  const val VOLTAGE_RANGE_MAX: Float = 5.0f
  
  const val HYSTERESIS_MIN: Float = 0.0f
  const val HYSTERESIS_MAX: Float = 1.0f

  const val MAX_SPEED_MIN: Int = 0
  const val MAX_SPEED_MAX: Int = 500

  const val TREND_DURATION_MIN: Int = 0
  const val TREND_DURATION_MAX: Int = 500

  const val STABLE_DURATION_MIN: Int = 0
  const val STABLE_DURATION_MAX: Int = 500
}

enum class BTParam {
  B_CHIP_FREQ,
  B_PREDICTION_HORIZON,
  B_SPEED_MIN,
  B_SPEED_MAX,
  B_GYRO_HYSTERESIS,
  B_TARGET_PITCH,
  B_DEAD_ZONE,
  B_ENTER_THRESHOLD,
  B_EMERG_THRESHOLD,
  B_HYSTERESIS,
  B_MAX_SPEED,
  B_TREND_DURATION,
  B_STABLE_DURATION,
  B_REVERSED_PITCH,
  B_REVERSED_ROLL,

  B_CONNECTED,

  B_ROLL,
  B_PITCH,
  B_VOLTAGE_IN,
  B_VOLTAGE_OUT,
  B_VOLTAGE_RANGE_MIN,
  B_VOLTAGE_RANGE_MAX,
  B_CHIP_TEMP,

  B_GET_SETTINGS,
  B_SET_SETTINGS,

  B_GET_VOLTAGE_RANGE,
  B_RESET_VOLTAGE,

  B_GET_VOLTAGE,
  B_GET_POSITION,

  B_CALIBRATE_GYRO,
  B_CALIBRATE_GYRO_PROG,

  B_OTA_START,
  B_OTA_SUCCESS,
  B_OTA_ERROR,
  B_OTA_MESSAGE,
  B_OTA_PROGRESS,

  B_FIRMWARE_URL,
  B_FIRMWARE_VERSION,

  B_WIFI_SSID,
  B_WIFI_PASS,
  B_WIFI_TIMEOUT,

  B_WIFI_CONNECTING,
  B_WIFI_SUCCESS,
  B_WIFI_ERROR,

  B_GET_STATE,
  B_SET_STATE;

  override fun toString(): String = this.ordinal.toString()
  fun toInt(): Int = this.ordinal
  companion object {
    fun fromInt(ordinal: Int): BTParam? = entries.getOrNull(ordinal)
  }
}
