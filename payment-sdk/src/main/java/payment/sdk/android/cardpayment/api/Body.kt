package payment.sdk.android.cardpayment.api

import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URLEncoder

abstract class Body(protected val parameters: Map<String, String>) {

    abstract fun encode(): String

    fun isNotEmpty(): Boolean = parameters.isNotEmpty()

    class Json(parameters: Map<String, String>) : Body(parameters) {
        override fun encode() =
                JSONObject(parameters).toString()
    }

    class Form(parameters: Map<String, String>) : Body(parameters) {
        override fun encode() =
                StringBuilder().apply {
                    for (element in parameters) {
                        append(URLEncoder.encode(element.key, "UTF-8"))
                                .append('=')
                                .append(URLEncoder.encode(element.value, "UTF-8"))
                                .append('&')
                    }
                    deleteCharAt(length - 1)
                }.toString()
    }

    class Empty : Body(emptyMap()) {
        override fun encode() = ""

    }

    // use map with placeholder so that body is not consider as empty
    class JsonObject(private val jsonString: JSONObject) : Body(mapOf("placeholder" to "value")) {

        override fun encode() = jsonString.toString()
    }
}

