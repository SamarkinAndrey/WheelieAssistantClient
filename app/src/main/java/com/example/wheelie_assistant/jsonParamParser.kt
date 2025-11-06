import org.json.JSONObject
import org.json.JSONException
import android.util.Log

class JsonParamParser() {
  private val root = JSONObject()

  fun parse(jsonString: String): Boolean {
    return try {
      if (jsonString.isEmpty())
        return false

      clear()

      val newRoot = JSONObject(jsonString)
      newRoot.keys().forEach { key ->
        root.put(key, newRoot.get(key))
      }
      true
    } catch (e: JSONException) {
      Log.d("JsonParamParser", "Parse error: ${e.message}")
      false
    }
  }

  fun clear() {
    if (isEmpty()) return

    val keys = root.keys().asSequence().toList()
    keys.forEach { root.remove(it) }
  }

  fun getString(param: Int, defaultValue: String = ""): String {
    return root.optString(param.toString(), defaultValue)
  }

  fun getString(param: BTParam, defaultValue: String = ""): String {
    return getString(param.value, defaultValue)
  }

  fun getInt(param: Int, defaultValue: Int = 0): Int {
    return root.optInt(param.toString(), defaultValue)
  }

  fun getInt(param: BTParam, defaultValue: Int = 0): Int {
    return getInt(param.value, defaultValue)
  }

  fun getLong(param: Int, defaultValue: Long = 0L): Long {
    return root.optLong(param.toString(), defaultValue)
  }

  fun getLong(param: BTParam, defaultValue: Long = 0L): Long {
    return getLong(param.value, defaultValue)
  }

  fun getFloat(param: Int, defaultValue: Float = 0.0f): Float {
    return root.optDouble(param.toString(), defaultValue.toDouble()).toFloat()
  }

  fun getFloat(param: BTParam, defaultValue: Float = 0.0f): Float {
    return getFloat(param.value, defaultValue)
  }

  fun getDouble(param: Int, defaultValue: Double = 0.0): Double {
    return root.optDouble(param.toString(), defaultValue)
  }

  fun getDouble(param: BTParam, defaultValue: Double = 0.0): Double {
    return getDouble(param.value, defaultValue)
  }

  fun getBoolean(param: Int, defaultValue: Boolean = false): Boolean {
    return root.optBoolean(param.toString(), defaultValue)
  }

  fun getBoolean(param: BTParam, defaultValue: Boolean = false): Boolean {
    return getBoolean(param.value, defaultValue)
  }

  fun hasParam(param: Int): Boolean {
    return root.has(param.toString()) && !root.isNull(param.toString())
  }

  fun hasParam(param: BTParam): Boolean {
    return hasParam(param.value)
  }

  fun setString(param: Int, value: String) {
    root.put(param.toString(), value)
  }

  fun setString(param: BTParam, value: String) {
    setString(param.value, value)
  }

  fun setInt(param: Int, value: Int) {
    root.put(param.toString(), value)
  }

  fun setInt(param: BTParam, value: Int) {
    setInt(param.value, value)
  }

  fun setLong(param: Int, value: Long) {
    root.put(param.toString(), value)
  }

  fun setLong(param: BTParam, value: Long) {
    setLong(param.value, value)
  }

  fun setFloat(param: Int, value: Float) {
    root.put(param.toString(), value.toDouble())
  }

  fun setFloat(param: BTParam, value: Float) {
    setFloat(param.value, value)
  }

  fun setDouble(param: Int, value: Double) {
    root.put(param.toString(), value)
  }

  fun setDouble(param: BTParam, value: Double) {
    setDouble(param.value, value)
  }

  fun setBoolean(param: Int, value: Boolean) {
    root.put(param.toString(), value)
  }

  fun setBoolean(param: BTParam, value: Boolean) {
    setBoolean(param.value, value)
  }

  fun count(): Int {
    return root.length()
  }

  fun isEmpty(): Boolean {
    return root.length() == 0
  }

  fun serialize(compact: Boolean = true): String {
    return if (compact) {
      root.toString()
    } else {
      root.toString(2)
    }
  }
}

fun isJsonString(str: String): Boolean {
  return try {
    JSONObject(str)
    true
  } catch (e: JSONException) {
    false
  }
}
