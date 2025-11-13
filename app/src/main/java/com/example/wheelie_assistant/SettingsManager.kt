package com.app.wheelie_assistant

import org.json.JSONObject
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
    var server_ssid: String = "",
    var server_pass: String = "",
    var firmware_ver: String = ""
)

object SettingsManager {
    var currentSettings = Settings()
    
    fun updateFromParser(parser: JsonParamParser): Boolean {
        return try {
            currentSettings = Settings(
                system_tick = parser.getInt(B_SYSTEM_TICK, 0),
                prediction_horizont = parser.getInt(B_PREDICTION_HORIZONT, 0),
                gyro_hysteresis = parser.getFloat(B_GYRO_HYSTERESIS, 0f),
                reversed_pitch = parser.getBoolean(B_REVERSED_PITCH, false),
                reversed_roll = parser.getBoolean(B_REVERSED_ROLL, false),
                target_pitch = parser.getFloat(B_TARGET_PITCH, 0f),
                dead_zone = parser.getFloat(B_DEAD_ZONE, 0f),
                exit_threshold = parser.getFloat(B_EXIT_THRESHOLD, 0f),
                emerg_threshold = parser.getFloat(B_EMERG_THRESHOLD, 0f),
                min_voltage = parser.getFloat(B_MIN_VOLTAGE, 0f),
                min_step = parser.getInt(B_MIN_STEP, 0),
                max_step = parser.getInt(B_MAX_STEP, 0),
                hysteresis = parser.getFloat(B_HYSTERESIS, 0f),
                max_speed =  parser.getInt(B_MAX_SPEED, 0),
                trend_duration = parser.getInt(B_TREND_DURATION, 0),
                stable_duration = parser.getInt(B_STABLE_DURATION, 0),
                server_ssid = parser.getString(B_SERVER_SSID, ""),
                server_pass = parser.getString(B_SERVER_PASS, ""),
                firmware_ver = parser.getString(B_FIRMWARE_VERSION, "")
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun toJson(settings: Settings = currentSettings): String {
        return JSONObject().apply {
            put(B_SET_SETTINGS.s(), 1)
            put(B_SYSTEM_TICK.s(), settings.system_tick)
            put(B_PREDICTION_HORIZONT.s(), settings.prediction_horizont)
            put(B_GYRO_HYSTERESIS.s(), settings.gyro_hysteresis)
            put(B_REVERSED_PITCH.s(), settings.reversed_pitch)
            put(B_REVERSED_ROLL.s(), settings.reversed_roll)
            put(B_TARGET_PITCH.s(), settings.target_pitch)
            put(B_DEAD_ZONE.s(), settings.dead_zone)
            put(B_EXIT_THRESHOLD.s(), settings.exit_threshold)
            put(B_EMERG_THRESHOLD.s(), settings.emerg_threshold)
            put(B_MIN_VOLTAGE.s(), settings.min_voltage)
            put(B_MIN_STEP.s(), settings.min_step)
            put(B_MAX_STEP.s(), settings.max_step)
            put(B_HYSTERESIS.s(), settings.hysteresis)
            put(B_MAX_SPEED.s(), settings.max_speed)
            put(B_TREND_DURATION.s(), settings.trend_duration)
            put(B_STABLE_DURATION.s(), settings.stable_duration)
            put(B_SERVER_SSID.s(), settings.server_ssid)
            put(B_SERVER_PASS.s(), settings.server_pass)
        }.toString()
    }

    fun validate(settings: Settings = currentSettings): Boolean {
        return (settings.system_tick in 5..100) &&
            (settings.prediction_horizont in settings.system_tick..250) &&
            (settings.gyro_hysteresis in 0.0f..5.0f) &&
            (settings.reversed_pitch in false..true) &&
            (settings.reversed_roll in false..true) &&
            (settings.target_pitch in 15.0f..65.0f) &&
            (settings.dead_zone in 0.0f..5.0f) &&
            (settings.exit_threshold in 0.0f..10.0f) &&
            (settings.emerg_threshold in 0.0f..10.0f) &&
            (settings.min_voltage in 0.0f..5.0f) &&
            (settings.min_step in 1..10) &&
            (settings.max_step in settings.min_step..10) &&
            (settings.hysteresis in 0.0f..1.0f) &&
            (settings.max_speed in 0..500) &&
            (settings.trend_duration in 0..500) &&
            (settings.stable_duration in 0..500)
    }
}