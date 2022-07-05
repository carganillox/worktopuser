package org.drinklink.app.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 *
 */

public class Analytics {

    private FirebaseAnalytics firebaseAnalytics;
    private String tag;

    public Analytics(Context context, String tag) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.tag = tag;
    }

    public void signUp() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }

    public void addToCart() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
    }

    public void removeFromChart() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.REMOVE_FROM_CART, bundle);
    }

    public void appOpen() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    public void beginCheckout() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle);
    }

    public void login() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    public void refund() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.REFUND, bundle);
    }

    public void purchase() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle);
    }

    public void selectDiscount() {
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_PROMOTION, bundle);
    }

    public void payed() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "payed");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void accepted() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "accepted");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void rejected() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "rejected");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void readyToCollect() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "readyToCollect");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void already2Orders() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "already2Orders");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void invalidBilling() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "invalidBilling");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void invalidEmail() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "invalidEmail");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void invalidPaymentDetails() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "invalidPaymentDetails");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void paymentFailed() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "paymentFailed");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

    public void collected() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL_NAME, "collected");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }

//    public void test() {
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
//        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
//        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//    }
}
