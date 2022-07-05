package payment.sdk.android.cardpayment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import payment.sdk.android.cardpayment.api.CoroutinesGatewayHttpClient
import payment.sdk.android.cardpayment.threedsecure.ThreeDSecureRequest
import payment.sdk.android.cardpayment.threedsecure.ThreeDSecureWebViewActivity
import payment.sdk.android.core.CardType
import payment.sdk.android.core.dependency.StringResourcesImpl
import payment.sdk.android.sdk.R

class CardPaymentFragment() : Fragment(), CardPaymentContract.Interactions {

    private lateinit var presenter: CardPaymentContract.Presenter
    private lateinit var rootView: View
    private lateinit var callback: PaymentFinished

    constructor( callback: PaymentFinished) : this() {
        this.callback = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.view_bottom_sheet, container, false)
        val view = CardPaymentView(rootView.findViewById(R.id.view_bottom_sheet_container), false)

        presenter = CardPaymentPresenter(
                view = view,
                interactions = this,
                paymentApiInteractor = CardPaymentApiInteractor(CoroutinesGatewayHttpClient()),
                stringResources = StringResourcesImpl(activity as Context)
        )
        presenter.onCardNumberFocusGained()
        fake()
        return rootView;
    }

    fun pay(url: String, code: String, savedCardPayment: SavedCardPayment) {
        presenter.pay(url, code, savedCardPayment)
    }

    fun getCardholderName() : String {
        return presenter.getCardholderName()
    }

    fun getCardNumber() : String {
        return presenter.getCardNumber()
    }

    fun getExpiry() : String {
        return presenter.getExpiry()
    }

    fun getCardType() : CardType {
        return presenter.getCardType()
    }

    fun isValid(): Boolean {
        return presenter.onValidateInputs()
    }

    override fun onStart3dSecure(threeDSecureRequest: ThreeDSecureRequest) {
        startActivityForResult(
                ThreeDSecureWebViewActivity.getIntent(
                        context = activity as Context,
                        acsUrl = threeDSecureRequest.acsUrl,
                        acsPaReq = threeDSecureRequest.acsPaReq,
                        acsMd = threeDSecureRequest.acsMd,
                        gatewayUrl = threeDSecureRequest.gatewayUrl),
                THREE_D_SECURE_REQUEST_KEY
        )
    }

    override fun onPaymentAuthorized() {
        finishWithData(CardPaymentData(CardPaymentData.STATUS_PAYMENT_AUTHORIZED))
    }

    override fun onPaymentCaptured() {
        finishWithData(CardPaymentData(CardPaymentData.STATUS_PAYMENT_CAPTURED))
    }

    override fun onPaymentFailed() {
        finishWithData(CardPaymentData(CardPaymentData.STATUS_PAYMENT_FAILED))
    }

    override fun onGenericError(message: String?) {
        finishWithData(CardPaymentData(CardPaymentData.STATUS_GENERIC_ERROR, message))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == THREE_D_SECURE_REQUEST_KEY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                presenter.onHandle3DSecurePaymentSate(data.getStringExtra(ThreeDSecureWebViewActivity.KEY_3DS_STATE).toString())
            } else {
                onPaymentFailed()
            }
        }
    }

    private fun finishWithData(cardPaymentData: CardPaymentData) {
        callback.finished(cardPaymentData)
    }

    fun fake() {
        presenter.fake()
    }

    companion object {

        private const val THREE_D_SECURE_REQUEST_KEY: Int = 100

        private const val URL_KEY = "gateway-payment-url"
        private const val CODE = "code"


        fun getIntent(context: Context, url: String, code: String) =
                Intent(context, CardPaymentFragment::class.java).apply {
                    putExtra(URL_KEY, url)
                    putExtra(CODE, code)

                }

    }
}
