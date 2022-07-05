package payment.sdk.android.cardpayment

class SavedCardPayment constructor(
        val scheme: String,
        val cardToken: String,
        val cardHolderName: String,
        val maskedPan: String,
        val email: String,
        val firstName : String,
        val lastName: String,
        val expiry: String,
        val currency: String,
        val amount: Int,
        val orderId: Int
) {
    var isInitialized : Boolean = true

    constructor() : this("", "", "", "", "", "", "", "", "", -1, -1) {
        this.isInitialized = false
    }
}