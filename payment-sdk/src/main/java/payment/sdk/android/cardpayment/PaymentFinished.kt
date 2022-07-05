package payment.sdk.android.cardpayment

interface PaymentFinished {

    fun finished(result: CardPaymentData)
}