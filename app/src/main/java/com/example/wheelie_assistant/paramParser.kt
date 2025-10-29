import org.json.JSONObject
import org.json.JSONException

class paramParser {
  data class Parameter(val name: String, val value: String? = null)

  companion object {
    private const val MAX_PARAMS = 100
  }

  private val params = mutableListOf<Parameter>()
  val paramCount: Int get() = params.size

  fun parse(input: String, delimiter: Char = ','): Boolean {
    params.clear()

    val buffer = input.trim()
    if (buffer.isEmpty()) return false

    buffer.splitToSequence(delimiter).take(MAX_PARAMS).forEach { pair -> parsePair(pair) }

    return params.isNotEmpty()
  }

  fun getValue(paramName: String, defaultValue: String? = null): String? {
    return params.find { it.name.equals(paramName, ignoreCase = true) }?.value ?: defaultValue
  }

  fun hasParam(paramName: String): Boolean {
    return params.any { it.name.equals(paramName, ignoreCase = true) }
  }

  fun getParam(paramNum: Int): Parameter? {
    return params.getOrNull(paramNum)
  }

  fun getAllParams(): List<Parameter> {
    return params.toList()
  }

  fun toJson(): String {
    return try {
      val jsonObject = JSONObject()
      params.forEach { param ->
        jsonObject.put(param.name, param.value ?: JSONObject.NULL)
      }
      jsonObject.toString()
    } catch (e: JSONException) {
      ""
    }
  }

  private fun String.removeQuotes(quote: String): String {
    return if (this.length >= 2 && this.startsWith(quote) && this.endsWith(quote)) {
      this.substring(1, this.length - 1)
    } else {
      this
    }
  }

  private fun parsePair(pair: String, delimiter: Char = '=') {
    val equalsPos = pair.indexOf(delimiter)

    if (equalsPos == -1) {
      params.add(Parameter(pair.trim()))
      return
    }

    val name = pair.substring(0, equalsPos).trim()
    var value = pair.substring(equalsPos + 1).trim()

    value = value.removeQuotes("\"")

    params.add(Parameter(name, value))
  }
}
