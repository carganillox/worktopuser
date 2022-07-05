package org.drinklink.app.payment;

import android.content.Context;
import android.content.Intent;

//import com.paytabs.paytabs_sdk.payment.ui.activities.PayTabActivity;
//import com.paytabs.paytabs_sdk.utils.PaymentParams;

import org.drinklink.app.R;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.PaymentOption;
import org.drinklink.app.ui.activities.MainActivity;

import lombok.Data;

/**
 *
 */
public class PayTabPaymentManager {

    private static final String TAG = "PayTabPaymentManager";

    private static final String MANGO_TANGO_COLOR_CODE = "#f6834a";

    public static Intent preparePaymentIntent(Context context, Order order, PaymentOption billingAddress) {
        Intent in = prepareBaseIntent(context, order);

//        in.putExtra(PaymentParams.TOKEN, billingAddress.getToken());
//        in.putExtra(PaymentParams.CUSTOMER_EMAIL, billingAddress.getEmail());
//        in.putExtra(PaymentParams.CUSTOMER_PASSWORD, billingAddress.getPassword());

        return in;
    }

    public static Intent prepareTokenizationIntent(Context context, Order order, PaymentOption billingAddress) {
        Intent in = preparePaymentIntentWithoutTokenization(context, order, billingAddress);
        //Tokenization
//        in.putExtra(PaymentParams.IS_TOKENIZATION, true);
        return in;
    }

    public static Intent preparePaymentIntentWithoutTokenization(Context context, Order order, PaymentOption billingAddress) {
        Intent in = prepareBaseIntent(context, order);

//        in.putExtra(PaymentParams.CUSTOMER_PHONE_NUMBER, billingAddress.getPhoneNumber());
//        in.putExtra(PaymentParams.CUSTOMER_EMAIL, billingAddress.getEmail());
//
//        //Billing Address
//        in.putExtra(PaymentParams.ADDRESS_BILLING, billingAddress.getAddress());
//        in.putExtra(PaymentParams.CITY_BILLING, billingAddress.getCity());
//        in.putExtra(PaymentParams.STATE_BILLING, billingAddress.getState());
//        in.putExtra(PaymentParams.COUNTRY_BILLING, billingAddress.getCountry());
//        in.putExtra(PaymentParams.POSTAL_CODE_BILLING, billingAddress.getPostalCodeOrCountryPhoneCode()); //Put Country Phone code if Postal code not available '00973'
//        //Shipping Address
//        in.putExtra(PaymentParams.ADDRESS_SHIPPING, billingAddress.getAddress());
//        in.putExtra(PaymentParams.CITY_SHIPPING, billingAddress.getCity());
//        in.putExtra(PaymentParams.STATE_SHIPPING, billingAddress.getState());
//        in.putExtra(PaymentParams.COUNTRY_SHIPPING, billingAddress.getCountry());
//        in.putExtra(PaymentParams.POSTAL_CODE_SHIPPING, billingAddress.getPostalCodeOrCountryPhoneCode()); //Put Country Phone code if Postal code not available '00973'
        // PRE AUTH.
//        in.putExtra(PaymentParams.IS_PREAUTH, true);
        return in;
    }

    private static Intent prepareBaseIntent(Context context, Order order) {
//        Intent in = new Intent(context, PayTabActivity.class);
        Intent in = new Intent(context, MainActivity.class);
//        in.putExtra(PaymentParams.MERCHANT_EMAIL, "info@drinklink.ae"); //this a demo account for testing the sdk
//        in.putExtra(PaymentParams.SECRET_KEY, "20jRTF1P8PwUtvLYDXfaSyPFG4YJIXOKY7BSNDHVtdQXJCVmzbxsJn7qr5aa2YV06xSTbAPEOGBxixUSJrktPsUhx4rA3nB9Ut35");//Add your Secret Key Here
//        in.putExtra(PaymentParams.LANGUAGE, PaymentParams.ENGLISH);
//        in.putExtra(PaymentParams.TRANSACTION_TITLE, context.getString(R.string.paytabs_transaction_title, order.getId()));
//        in.putExtra(PaymentParams.AMOUNT, order.getFinalPrice());
//        in.putExtra(PaymentParams.CURRENCY_CODE, order.getCurrency());
//        in.putExtra(PaymentParams.ORDER_ID, Integer.toString(order.getId()));
//        in.putExtra(PaymentParams.PRODUCT_NAME, order.getProductName());
//        //Payment Page Style
//        in.putExtra(PaymentParams.PAY_BUTTON_COLOR, MANGO_TANGO_COLOR_CODE);
        return in;
    }

    public static PaymentResult processPaymentResult(int resultCode, Intent data) {
        PaymentResult paymentResult = new PaymentResult();
        if (resultCode == ExtrasKey.RESULT_OK) {
//            paymentResult.setResultMessage(data.getStringExtra(PaymentParams.RESULT_MESSAGE));
//            paymentResult.setResponseCode(data.getStringExtra(PaymentParams.RESPONSE_CODE));
//            paymentResult.setTransactionId(data.getStringExtra(PaymentParams.TRANSACTION_ID));
//            paymentResult.setToken(data.getStringExtra(PaymentParams.TOKEN));
//            if (paymentResult.isTokenized()) {
//                paymentResult.setEmail(data.getStringExtra(PaymentParams.CUSTOMER_EMAIL));
//                paymentResult.setPassword(data.getStringExtra(PaymentParams.CUSTOMER_PASSWORD));
//            }
        }
        return paymentResult;
    }

    @Data
    public static class PaymentResult {

        private static final String SUCCESS_RESPONSE_CODE = "100";

        private String email;
        private String password;
        private String token;
        private String responseCode;
        private String transactionId;
        private String resultMessage;

        public boolean isSuccessful() {
            return responseCode == SUCCESS_RESPONSE_CODE;
        }

        public boolean isTokenized() {
            return token != null && !token.isEmpty();
        }

    }
}
