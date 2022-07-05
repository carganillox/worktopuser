package payment.sdk.android.cardpayment

import payment.sdk.android.core.CardType
import payment.sdk.android.core.OrderAmount
import org.json.JSONObject

interface PaymentApiInteractor {

    fun authorizePayment(
            url: String,
            code: String,
            success: (List<String>, String) -> Unit,
            error: (Exception) -> Unit)

    fun getOrder(
            orderUrl: String,
            paymentCookie: String,
            success: (String, String, Set<CardType>, OrderAmount, String) -> Unit,
            error: (Exception) -> Unit)

    fun doPayment(
            paymentUrl: String,
            paymentCookie: String,
            pan: String,
            expiry: String,
            cvv: String,
            cardHolder: String,
            success: (state: String, response: JSONObject) -> Unit,
            error: (Exception) -> Unit)

    fun doSavedCardPayment(
            savedCardPaymentUrl: String,
            paymentCookie: String,
            savedCardPayment: SavedCardPayment,
            success: (state: String, response: JSONObject) -> Unit,
            error: (Exception) -> Unit)
}