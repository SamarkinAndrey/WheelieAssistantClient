class BLEParser {
  data class BLEParameter(val id: UByte, val type: UByte, val value: Float)

  companion object {
      private const val MAX_PARAMS = 100
      private const val PARAM_SIZE = 6
  }

  private val params = mutableListOf<BLEParameter>()
  val paramCount: Int get() = params.size

  fun parse(data: ByteArray): Boolean {
      params.clear()
      if (data.size < PARAM_SIZE) return false

      var offset = 0
      while (offset + PARAM_SIZE <= data.size && params.size < MAX_PARAMS) {
          try {
              val id = data[offset].toUByte()
              val type = data[offset + 1].toUByte()
              
              // РУЧНОЙ разбор float для СОВМЕСТИМОСТИ с C++
              val value = bytesToFloat(data, offset + 2)
              
              params.add(BLEParameter(id, type, value))
              offset += PARAM_SIZE
          } catch (e: Exception) {
              // При любой ошибке - пропускаем этот блок и двигаемся к следующему
              offset += PARAM_SIZE
              continue
          }
      }

      return params.isNotEmpty()
  }

  fun getValue(paramId: UByte, defaultValue: Float = 0.0f): Float {
      return params.find { it.id == paramId }?.value ?: defaultValue
  }

  fun getType(paramId: UByte): UByte {
      return params.find { it.id == paramId }?.type ?: 0u
  }

  fun hasParam(paramId: UByte): Boolean {
      return params.any { it.id == paramId }
  }

  fun getParam(paramNum: Int): BLEParameter? {
      return params.getOrNull(paramNum)
  }

  fun getAllParams(): List<BLEParameter> {
      return params.toList()
  }

  fun setValue(paramId: UByte, value: Float, type: UByte = 0u): Boolean {
      val existingIndex = params.indexOfFirst { it.id == paramId }
      
      return if (existingIndex != -1) {
          params[existingIndex] = BLEParameter(paramId, type, value)
          true
      } else if (params.size < MAX_PARAMS) {
          params.add(BLEParameter(paramId, type, value))
          true
      } else {
          false
      }
  }

  fun toBinary(): ByteArray {
      if (params.isEmpty()) return byteArrayOf()

      val result = ByteArray(params.size * PARAM_SIZE)
      
      params.forEachIndexed { index, param ->
          val offset = index * PARAM_SIZE
          result[offset] = param.id.toByte()
          result[offset + 1] = param.type.toByte()
          
          // РУЧНАЯ упаковка float для СОВМЕСТИМОСТИ
          floatToBytes(param.value, result, offset + 2)
      }

      return result
  }

  fun getBinarySize(): Int {
      return params.size * PARAM_SIZE
  }

  fun clear() {
      params.clear()
  }

  fun removeParam(paramId: UByte): Boolean {
      return params.removeIf { it.id == paramId }
  }

  // 🔧 РУЧНЫЕ ФУНКЦИИ ДЛЯ СОВМЕСТИМОСТИ С C++
  private fun bytesToFloat(data: ByteArray, offset: Int): Float {
      // АНАЛОГ memcpy() из C++ - нативный порядок байт системы
      var bits = 0
      for (i in 0..3) {
          bits = bits or ((data[offset + i].toInt() and 0xFF) shl (8 * i))
      }
      return Float.fromBits(bits)
  }

  private fun floatToBytes(value: Float, target: ByteArray, offset: Int) {
      // АНАЛОГ memcpy() из C++ - нативный порядок байт системы
      val bits = value.toBits()
      for (i in 0..3) {
          target[offset + i] = ((bits shr (8 * i)) and 0xFF).toByte()
      }
  }

  override fun toString(): String {
      val sb = StringBuilder()
      sb.appendLine("=== BLE Parameters ===")
      params.forEach { param ->
          sb.appendLine("ID: 0x${param.id.toString(16).padStart(2, '0').uppercase()}, " +
                       "Type: 0x${param.type.toString(16).padStart(2, '0').uppercase()}, " +
                       "Value: ${"%.2f".format(param.value)}")
      }
      sb.appendLine("Total: $paramCount params, Binary size: ${getBinarySize()} bytes")
      sb.appendLine("======================")
      return sb.toString()
  }
}