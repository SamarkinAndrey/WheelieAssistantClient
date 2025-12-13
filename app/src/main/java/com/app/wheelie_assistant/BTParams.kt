package com.app.wheelie_assistant

import kotlin.String

object Defaults {
  const val SYSTEM_TICK: Int = 10
  const val PREDICT_HORIZONT: Int = 90

  const val GYRO_HYSTERESIS: Float = 0.2f

  const val TARGET_PITCH: Float = 25.0f
  const val DEAD_ZONE: Float = 0.5f
  const val EXIT_THRESHOLD: Float = 5.0f
  const val EMERG_THRESHOLD: Float = 5.0f

  const val MIN_VOLTAGE: Float = 1.5f

  const val CHIP_FREQ: Int = 80

  const val MIN_STEP: Int = 1
  const val MAX_STEP: Int = 4

  const val HYSTERESIS: Float = 0.3f
  const val MAX_SPEED: Int = 200
  const val TREND_DURATION: Int = 50
  const val STABLE_DURATION: Int = 300
}

object Limits {
  const val SYSTEM_TICK_MIN: Int = 10
  const val SYSTEM_TICK_MAX: Int = 100

  const val PREDICT_HORIZONT_MIN: Int = SYSTEM_TICK_MIN
  const val PREDICT_HORIZONT_MAX: Int = 250

  const val TARGET_PITCH_MIN: Float = 15.0f
  const val TARGET_PITCH_MAX: Float = 65.0f

  const val DEAD_ZONE_MIN: Float = 0.0f
  const val DEAD_ZONE_MAX: Float = 5.0f

  const val EXIT_THRESHOLD_MIN: Float = 0.0f
  const val EXIT_THRESHOLD_MAX: Float = 10.0f

  const val EMERG_THRESHOLD_MIN: Float = 0.0f
  const val EMERG_THRESHOLD_MAX: Float = 10.0f

  const val MIN_VOLTAGE_MIN: Float = 0.0f
  const val MIN_VOLTAGE_MAX: Float = 5.0f

  const val CHIP_FREQ_MIN: Int = 80
  const val CHIP_FREQ_MAX: Int = 240

  const val STEP_MIN: Int = 1
  const val STEP_MAX: Int = 10

  const val HYSTERESIS_MIN: Float = 0.0f
  const val HYSTERESIS_MAX: Float = 1.0f

  const val MAX_SPEED_MIN: Int = 0
  const val MAX_SPEED_MAX: Int = 500

  const val TREND_DURATION_MIN: Int = 0
  const val TREND_DURATION_MAX: Int = 500

  const val STABLE_DURATION_MIN: Int = 0
  const val STABLE_DURATION_MAX: Int = 500

  const val GYRO_HYSTERESIS_MIN: Float = 0.0f
  const val GYRO_HYSTERESIS_MAX: Float = 5.0f
}

enum class BTParam {
  B_SYSTEM_TICK,
  B_PREDICTION_HORIZONT,
  B_GYRO_HYSTERESIS,
  B_REVERSED_PITCH,
  B_REVERSED_ROLL,
  B_TARGET_PITCH,
  B_DEAD_ZONE,
  B_EXIT_THRESHOLD,
  B_EMERG_THRESHOLD,
  B_MIN_VOLTAGE,
  B_MIN_STEP,
  B_MAX_STEP,
  B_HYSTERESIS,
  B_MAX_SPEED,
  B_TREND_DURATION,
  B_STABLE_DURATION,

  B_CONNECTED,

  B_ROLL,
  B_PITCH,
  B_VOLTAGE_IN,
  B_VOLTAGE_OUT,
  B_VOLTAGE_MIN,
  B_VOLTAGE_MAX,
  B_CHIP_FREQ,
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
