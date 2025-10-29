import android.widget.Toast
import org.json.JSONObject
import org.json.JSONException
import android.content.Context

class JsonParamParser(private val context: Context) {
  private val root = JSONObject()

  fun parse(jsonString: String): Boolean {
    return try {
      if (jsonString.isEmpty()) return false

      val keys = root.keys().asSequence().toList()
      keys.forEach { root.remove(it) }

      val newRoot = JSONObject(jsonString)
      newRoot.keys().forEach { key ->
        root.put(key, newRoot.get(key))
      }
      true
    } catch (e: JSONException) {
      Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
      false
    }
  }

  // Методы для Int параметров
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

  // Set методы
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

//import android.widget.Toast
//import org.json.JSONObject
//import org.json.JSONArray
//import org.json.JSONException
//import android.content.Context
//import android.util.Log
//
//class jsonParamParser (private val context: Context) {
//  private val root = JSONObject()
//
//  fun parse(jsonString: String): Boolean {
//    return try {
//      clear()
//      val newRoot = JSONObject(jsonString.trim())
//      newRoot.keys().forEach { key ->
//        root.put(key, newRoot.get(key))
//      }
//      true
//    } catch (e: JSONException) {
//      Log.e("jsonParamParser", jsonString, e)
//      Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
//      false
//    }
//  }
//
//  private fun resolvePath(path: String): Pair<JSONObject, String>? {
//    if (path.isEmpty()) return null
//
//    val keys = path.split('.')
//    var current = root
//
//    for (i in 0 until keys.size - 1) {
//      val key = keys[i]
//
//      if (!current.has(key)) return null
//      val next = current.optJSONObject(key) ?: return null
//      current = next
//    }
//
//    return Pair(current, keys.last())
//  }
//
//  fun getString(path: String, default: String = "") =
//    resolvePath(path)?.let { (obj, key) -> obj.optString(key, default) } ?: default
//
//  fun getInt(path: String, default: Int = 0) =
//    resolvePath(path)?.let { (obj, key) -> obj.optInt(key, default) } ?: default
//
//  fun getDouble(path: String, defaultValue: Double = 0.0) =
//    resolvePath(path)?.let { (obj, key) -> obj.optDouble(key, defaultValue) } ?: defaultValue
//
//  fun getFloat(path: String, defaultValue: Float = 0.0f): Float {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.optDouble(key, defaultValue.toDouble()).toFloat()
//    } ?: defaultValue
//  }
//
//  fun getBoolean(path: String, default: Boolean = false) =
//    resolvePath(path)?.let { (obj, key) -> obj.optBoolean(key, default) } ?: default
//
//  fun getObject(path: String): jsonParamParser? {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.optJSONObject(key)?.let { nestedObj ->
//        jsonParamParser(context).apply {
//          this.parse(nestedObj.toString())
//        }
//      }
//    }
//  }
//
//  fun getArray(path: String): List<Any?>? {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.optJSONArray(key)?.let { jsonArray ->
//        (0 until jsonArray.length()).map { index ->
//          when (val item = jsonArray.get(index)) {
//            is JSONObject -> jsonParamParser(context).apply {
//              this.parse(item.toString())
//            }
//            is JSONArray -> {
//              (0 until item.length()).map { item.get(it) }
//            }
//            else -> item
//          }
//        }
//      }
//    }
//  }
//
//  fun setString(path: String, value: String) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun setInt(path: String, value: Int) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun setBoolean(path: String, value: Boolean) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun setDouble(path: String, value: Double) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun setFloat(path: String, value: Float) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun setObject(path: String, obj: jsonParamParser) {
//    setValue(path) { parent, key ->
//      parent.put(key, JSONObject(obj.root.toString()))
//    }
//  }
//
//  fun setArray(path: String, array: List<Any?>) {
//    setValue(path) { obj, key ->
//      obj.put(key, JSONArray(array))
//    }
//  }
//
//  fun hasParam(path: String): Boolean {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.has(key)
//    } ?: false
//  }
//
//  fun toJsonString(pretty: Boolean = false): String {
//    return if (pretty) {
//      root.toString(2)
//    } else {
//      root.toString()
//    }
//  }
//
//  fun getAllParams(): Set<String> {
//    return root.keys().asSequence().toSet()
//  }
//
//  fun getParamsAtPath(path: String): Set<String>? {
//    return getObject(path)?.getAllParams()
//  }
//
//  fun clear() {
//    val keys = root.keys().asSequence().toList()
//    keys.forEach { key ->
//      root.remove(key)
//    }
//  }
//
//  fun remove(path: String): Boolean {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.remove(key)
//      true
//    } ?: false
//  }
//
//  private fun setValue(path: String, action: (JSONObject, String) -> Unit) {
//    if (path.isEmpty()) return
//
//    val keys = path.split('.')
//    var currentObj = root
//
//    for (i in 0 until keys.size - 1) {
//      val key = keys[i]
//      var nextObj = currentObj.optJSONObject(key)
//      if (nextObj == null) {
//        if (currentObj.has(key) && !currentObj.isNull(key)) {
//          throw IllegalArgumentException("Path '$path' conflicts with existing non-object value at '$key'")
//        }
//        nextObj = JSONObject()
//        currentObj.put(key, nextObj)
//      }
//      currentObj = nextObj
//    }
//
//    action(currentObj, keys.last())
//  }
//
//  fun merge(other: jsonParamParser, overwrite: Boolean = true) {
//    other.getAllParams().forEach { key ->
//      if (overwrite || !root.has(key)) {
//        root.put(key, other.root.get(key))
//      }
//    }
//  }
//
//  fun merge(jsonObject: JSONObject, overwrite: Boolean = true) {
//    jsonObject.keys().forEach { key ->
//      if (overwrite || !root.has(key)) {
//        root.put(key, jsonObject.get(key))
//      }
//    }
//  }
//
//  fun getLong(path: String, default: Long = 0L): Long {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.optLong(key, default)
//    } ?: default
//  }
//
//  fun setLong(path: String, value: Long) {
//    setValue(path) { obj, key -> obj.put(key, value) }
//  }
//
//  fun isNull(path: String): Boolean {
//    return resolvePath(path)?.let { (obj, key) ->
//      obj.isNull(key)
//    } ?: true
//  }
//
//  fun isEmpty(): Boolean {
//    return root.length() == 0
//  }
//
//  fun size(): Int {
//    return root.length()
//  }
//}