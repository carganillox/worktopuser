package payment.sdk.android.cardpayment

import payment.sdk.android.cardpayment.threedsecure.ThreeDSecureRequest
import payment.sdk.android.core.OrderAmount
import payment.sdk.android.core.CardType


interface CardPaymentContract {

    interface View {
        val cardNumber: StatefulInput

        val expireDate: StatefulInput

        val cvv: StatefulInput

        val cardHolder: StatefulInput

        fun setPresenter(presenter: Presenter)

        fun setFloatingHintText(text: String)

        fun setCardNumberPreviewText(text: String)

        fun setExpireDatePreviewText(text: String)

        fun focusInCardNumber()

        fun focusInCardExpire()

        fun focusInCardHolder()

        fun focusInCvv()

        fun updateCardInputMask(mask: String)

        fun updateCardLogo(resourceId: Int?)

        fun updateCvvInputMask(mask: String)

        fun showCardFrontFace(onAnimationEndCallback: (() -> Unit)? = null)

        fun showCardBackFace(onAnimationEndCallback: (() -> Unit)? = null)

        fun showFrontCvvGuide(show: Boolean)

        fun showTopErrorMessage(show: Boolean)

        fun setTopErrorMessage(error: String)

        fun showBottomErrorMessage(show: Boolean)

        fun setBottomErrorMessage(error: String)

        fun showProgress(show: Boolean, text: String? = null)

        fun showProgressTimeOut(text: String?, timeout: () -> Unit)

        fun setFloatingHintTextVisible(visible: Boolean)

        fun fake()

        fun showButton()
    }

    interface Presenter {
        fun init()

        fun onCardNumberFocusGained()

        fun onExpireDateFocusGained()

        fun onCvvFocusGained()

        fun onCardNumberFocusLost()

        fun onExpireDateFocusLost()

        fun onCvvFocusLost()

        fun onCardNumberChanged(rawText: String, maskedText: String)

        fun onExpireDateChanged(rawText: String, maskedText: String)

        fun onCvvChanged(rawText: String, maskedText: String)

        fun onValidateInputs(): Boolean

        fun onPayClicked()

        fun onHandle3DSecurePaymentSate(state: String)

        fun getOrderInfo(): OrderAmount

        fun pay(url: String, code: String, savedCardPayment : SavedCardPayment)

        fun fake()

        fun getCardholderName() : String

        fun getCardNumber() : String

        fun getExpiry() : String

        fun getCardType() : CardType

        fun showButton()
    }

    interface Interactions {
        fun onStart3dSecure(threeDSecureRequest: ThreeDSecureRequest)

        fun onPaymentAuthorized()

        fun onPaymentCaptured()

        fun onPaymentFailed()

        fun onGenericError(message: String?)
    }

    interface StatefulInput {

        val full: Boolean

        val rawTxt: String

        val dirty: Boolean

        val txt: String

        fun setErrorWhen(predicate: StatefulInput.() -> Boolean): Boolean

        fun setText(s: String)
    }
}