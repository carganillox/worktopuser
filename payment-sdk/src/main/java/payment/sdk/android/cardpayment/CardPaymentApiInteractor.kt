package payment.sdk.android.cardpayment

import payment.sdk.android.cardpayment.api.Body
import payment.sdk.android.cardpayment.api.HttpClient
import android.net.Uri
import androidx.annotation.VisibleForTesting
import org.json.JSONObject
import payment.sdk.android.core.*

internal class CardPaymentApiInteractor(private val httpClient: HttpClient) : PaymentApiInteractor {

    override fun authorizePayment(url: String, code: String, success: (List<String>, String) -> Unit, error: (Exception) -> Unit) {
        httpClient.post(
                url = url,
                headers = mapOf(
                        HEADER_ACCEPT to "application/vnd.ni-payment.v2+json",
                        HEADER_CONTENT_TYPE to "application/x-www-form-urlencoded"
                ),
                body = Body.Form(mapOf(
                        "code" to code
                )),
                success = { (headers, response) ->
                    val cookies = headers[HEADER_SET_COOKIE]
                    val orderUrl = response.json("_links")?.json("cnp:order")
                            ?.string("href")
                    success(cookies!!, orderUrl!!)
                },
                error = { exception ->
                    error(exception)
                })
    }

    override fun getOrder(orderUrl: String, paymentCookie: String, success: (String, String, Set<CardType>, orderAmount: OrderAmount, String) -> Unit,
                          error: (Exception) -> Unit) {
        httpClient.get(
                url = orderUrl,
                headers = mapOf(
                        HEADER_COOKIE to paymentCookie
                ),
                success = { (_, response) ->
                    val orderReference = response.string("reference")
                    val paymentUrl = response.json("_embedded")
                            ?.array("payment")?.at(0)
                            ?.json("_links")?.json("payment:card")?.string("href")
                    val savedCardsPaymentUrl = response.json("_embedded")
                            ?.array("payment")?.at(0)
                            ?.json("_links")?.json("payment:saved-card")?.string("href")
                    val supportedCards = response.json("paymentMethods")
                            ?.array("card")?.toList<String>()

                    val orderValue = response.json("amount")!!.double("value")!!
                    val currencyCode = response.json("amount")!!.string("currencyCode")!!

                    val orderAmount = OrderAmount(orderValue, currencyCode)

                    success(orderReference!!, paymentUrl!!, CardMapping.mapSupportedCards(supportedCards!!), orderAmount,
                            savedCardsPaymentUrl.orEmpty())
                },
                error = { exception ->
                    error(exception)
                })
    }

    override fun doPayment(paymentUrl: String, paymentCookie: String, pan: String, expiry: String, cvv: String,
                           cardHolder: String, success: (state: String, response: JSONObject) -> Unit,
                           error: (Exception) -> Unit) {
        httpClient.put(
                url = paymentUrl,
                headers = mapOf(
                        HEADER_CONTENT_TYPE to "application/vnd.ni-payment.v2+json",
                        HEADER_ACCEPT to "application/vnd.ni-payment.v2+json",
                        HEADER_COOKIE to paymentCookie
                ),
                body = Body.Json(mapOf(
                        PAYMENT_FIELD_PAN to pan,
                        PAYMENT_FIELD_EXPIRY to expiry,
                        PAYMENT_FIELD_CVV to cvv,
                        PAYMENT_FIELD_CARDHOLDER to cardHolder
                )),
                success = { (_, response) ->
                    success(response.string("state")!!, response)
                },
                error = { exception ->
                    error(exception)
                })
    }

    override fun doSavedCardPayment(savedCardPaymentUrl: String,
                                    paymentCookie: String,
                                    savedCardPayment: SavedCardPayment,
                                    success: (state: String, response: JSONObject) -> Unit,
                                    error: (Exception) -> Unit) {

        var jsonBody = JSONObject()
        jsonBody.put("action", "AUTH")
        var jsonAmount = JSONObject()
        jsonAmount.put("currencyCode", savedCardPayment.currency)
        jsonAmount.put("value", savedCardPayment.amount)
        jsonBody.put("amount", jsonAmount)
        jsonBody.put("language", "en")
        jsonBody.put("emailAddress", savedCardPayment.email)
        var jsonBilling = JSONObject()
        jsonBilling.put("firstName", savedCardPayment.firstName)
        jsonBilling.put("lastName", savedCardPayment.lastName)
        jsonBody.put("billingAddress", jsonBilling)
        jsonBody.put("merchantOrderReference", savedCardPayment.orderId)
        var jsonMerchantAttribute = JSONObject()
        jsonMerchantAttribute.put("skip3DS", true)
        jsonBody.put("merchantAttributes", jsonMerchantAttribute)
        jsonBody.put("maskedPan", savedCardPayment.maskedPan)
        jsonBody.put("expiry", savedCardPayment.expiry)
        jsonBody.put("cardholderName", savedCardPayment.cardHolderName)
        jsonBody.put("scheme", savedCardPayment.scheme)
        jsonBody.put("cardToken", savedCardPayment.cardToken)
        jsonBody.put("recaptureCsc", true)
//
//        var jsonBody = JSONObject()
//        jsonBody.put("action", "AUTH")
//        var jsonAmount = JSONObject()
//        jsonAmount.put("currencyCode", "AED")
//        jsonAmount.put("value", 320)
//        jsonBody.put("amount", jsonAmount)
//        jsonBody.put("language", "en")
//        jsonBody.put("emailAddress", "testuser@gmail.com")
//        var jsonBilling = JSONObject()
//        jsonBilling.put("firstName", "Test")
//        jsonBilling.put("lastName", "User")
//        jsonBody.put("billingAddress", jsonBilling)
//        jsonBody.put("merchantOrderReference", 223)
//        var jsonMerchantAttribute = JSONObject()
//        jsonMerchantAttribute.put("skip3DS", true)
//        jsonBody.put("merchantAttributes", jsonMerchantAttribute)
//        jsonBody.put("maskedPan", "401200******1112")
//        jsonBody.put("expiry", "2021-11")
//        jsonBody.put("cardholderName", "TEST USER")
//        jsonBody.put("scheme", "VISA")
//        jsonBody.put("cardToken", "dG9rZW5pemVkUGFuLy92MS8vU0hPV19OT05FLy8wMTAwMjEwNDIxMTE0MTcz")
//        jsonBody.put("recaptureCsc", true)

//                "{\"action\":\"AUTH\"," +
//                        "\"amount\":{\"currencyCode\":\"AED\",\"value\":320}," +
//                        "\"language\":\"en\"," +
//                        "\"emailAddress\":\"testuser@gmail.com\"," +
//                        "\"billingAddress\":{\"firstName\":\"Test\",\"lastName\":\"User\"}," +
//                        "\"merchantOrderReference\":223," +
//                        "\"merchantAttributes\":{\"skip3DS\":true}," +
//                        "\"maskedPan\":\"401200******1112\"," +
//                        "\"expiry\":\"2021-11\"," +
//                        "\"cardholderName\":\"TEST USER\"," +
//                        "\"scheme\":\"VISA\"," +
//                        "\"cardToken\":\"dG9rZW5pemVkUGFuLy92MS8vU0hPV19OT05FLy8wMTAwMjEwNDIxMTE0MTcz\"," +
//                        "\"recaptureCsc\":true}"),

        httpClient.put(
                url = savedCardPaymentUrl,
                headers = mapOf(
                        HEADER_CONTENT_TYPE to "application/vnd.ni-payment.v2+json",
                        HEADER_ACCEPT to "application/vnd.ni-payment.v2+json",
                        HEADER_COOKIE to paymentCookie
                ),
                body = Body.JsonObject(jsonBody),
                success = { (_, response) ->
                    success(response.string("state")!!, response)
                },
                error = { exception ->
                    error(exception)
                })
    }



    companion object {
        @VisibleForTesting
        internal const val PAYMENT_FIELD_PAN = "pan"
        @VisibleForTesting
        internal const val PAYMENT_FIELD_EXPIRY = "expiry"
        @VisibleForTesting
        internal const val PAYMENT_FIELD_CVV = "cvv"
        @VisibleForTesting
        internal const val PAYMENT_FIELD_CARDHOLDER = "cardholderName"

        internal const val HEADER_ACCEPT = "Accept"
        internal const val HEADER_CONTENT_TYPE = "Content-Type"
        internal const val HEADER_COOKIE = "Cookie"
        internal const val HEADER_SET_COOKIE = "Set-Cookie"
    }
}


