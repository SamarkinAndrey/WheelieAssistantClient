package com.app.wheelie_assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlin.math.round

class MainViewModel : ViewModel() {

  enum class SystemState {
    IDLE,
    MONITORING,
    WHEELIE,
    EMERGENCY;

    override fun toString(): String = this.ordinal.toString()
    fun toInt(): Int = this.ordinal

    companion object {
      fun fromInt(value: Int): SystemState? = entries.getOrNull(value)
    }
  }

  enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED
  }

  private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
  val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

  private val _systemState = MutableStateFlow(SystemState.IDLE)
  val systemState: StateFlow<SystemState> = _systemState.asStateFlow()

  private val _pitch = MutableStateFlow(0f)
  val pitch: StateFlow<Float> = _pitch.asStateFlow()

  private val _roll = MutableStateFlow(0f)
  val roll: StateFlow<Float> = _roll.asStateFlow()

  private val _voltageIn = MutableStateFlow(0f)
  val voltageIn: StateFlow<Float> = _voltageIn.asStateFlow()

  private val _voltageOut = MutableStateFlow(0f)
  val voltageOut: StateFlow<Float> = _voltageOut.asStateFlow()

  private val _chipTemp = MutableStateFlow(0)
  val chipTemp: StateFlow<Int> = _chipTemp.asStateFlow()

  private val _voltageMin = MutableStateFlow(Float.POSITIVE_INFINITY)
  private val _voltageMax = MutableStateFlow(Float.NEGATIVE_INFINITY)

  private val _settingsRequested = MutableStateFlow(false)
  val settingsRequested: StateFlow<Boolean> = _settingsRequested.asStateFlow()

  val isConnecting: StateFlow<Boolean> = _connectionState.map { it == ConnectionState.CONNECTING }
    .stateIn(viewModelScope, SharingStarted.Lazily, false)

  val isConnected: StateFlow<Boolean> = _connectionState.map { it == ConnectionState.CONNECTED }
    .stateIn(viewModelScope, SharingStarted.Lazily, false)

  val stateIsIdle: StateFlow<Boolean> = _systemState.map { it == SystemState.IDLE }
    .stateIn(viewModelScope, SharingStarted.Lazily, true)

  val stateIsMonitoring: StateFlow<Boolean> = _systemState.map { it == SystemState.MONITORING }
    .stateIn(viewModelScope, SharingStarted.Lazily, false)

  val stateIsWheelie: StateFlow<Boolean> = _systemState.map { it == SystemState.WHEELIE }
    .stateIn(viewModelScope, SharingStarted.Lazily, false)

  val stateIsEmergency: StateFlow<Boolean> = _systemState.map { it == SystemState.EMERGENCY }
    .stateIn(viewModelScope, SharingStarted.Lazily, false)

  val normalizedVoltageIn: StateFlow<Float> = combine(
    _voltageIn, _voltageMin, _voltageMax
  ) { vin, min, max ->
    if (vin == 0f || max - min == 0f || max.isInfinite() || min.isInfinite()) 0f
    else (vin - min) / (max - min).coerceAtLeast(0.001f)
  }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

  val normalizedVoltageOut: StateFlow<Float> = combine(
    _voltageOut, _voltageMin, _voltageMax
  ) { vout, min, max ->
    if (vout == 0f || max - min == 0f || max.isInfinite() || min.isInfinite()) 0f
    else (vout - min) / (max - min).coerceAtLeast(0.001f)
  }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

  fun updateConnectionState(state: ConnectionState) {
    _connectionState.value = state
  }

  fun updateSystemState(state: SystemState) {
    _systemState.value = state
  }

  fun updateSensorData(
    pitch: Float,
    roll: Float,
    voltageIn: Float,
    voltageOut: Float,
    chipTemp: Int
  ) {
    _pitch.value = round(pitch * 10) / 10
    _roll.value = round(roll * 10) / 10
    _voltageIn.value = voltageIn
    _voltageOut.value = voltageOut
    _chipTemp.value = chipTemp

    if (voltageIn > 0 && voltageIn < _voltageMin.value) {
      _voltageMin.value = voltageIn
    }
    if (voltageOut > 0 && voltageOut < _voltageMin.value) {
      _voltageMin.value = voltageOut
    }
    if (voltageIn > _voltageMax.value) {
      _voltageMax.value = voltageIn
    }
    if (voltageOut > _voltageMax.value) {
      _voltageMax.value = voltageOut
    }
  }

  fun requestSettings() {
    _settingsRequested.value = true
  }

  fun settingsRequestProcessed() {
    _settingsRequested.value = false
  }

  fun clearVoltageRange() {
    _voltageMin.value = Float.POSITIVE_INFINITY
    _voltageMax.value = Float.NEGATIVE_INFINITY
  }

  fun clearSensorData() {
    _pitch.value = 0f
    _roll.value = 0f
    _voltageIn.value = 0f
    _voltageOut.value = 0f
    _chipTemp.value = 0
  }

  fun updateVoltageRange(min: Float, max: Float) {
    _voltageMin.value = min
    _voltageMax.value = max
  }
}