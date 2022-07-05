/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.PaymentOption;
import org.drinklink.app.persistence.model.SettingsPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class PreferencesStorage {

    String PREFERENCES_FILE = "org.drinklink.app.settings.xml";

    private static final String PAYMENT_OPTIONS_JSON_KEY = "payment_options_json";
    private static final String CARDS_JSON_KEY = "cards_json";
    private static final String BILL_EMAIL_KEY = "bill_email";
    private static final String BILL_FIRST_NAME_KEY = "bill_first_name";
    private static final String BILL_LAST_NAME_KEY = "bill_last_name";
    private static final String BILL_ADDRESS_KEY = "bill_address";
    private static final String BILL_TO_CARDHOLDER_KEY = "bill_to_cardholder";
    private static final String READY_SOUND_ON ="ready_sound_on";
    private static final String STATE_CHANGE_SOUND_ON = "state_change_sound_on";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String AUTH_REFRESH_TOKEN = "auth_refrehs_token";
    private static final String AUTH_USERNAME = "auth_username";
    private static final String AUTH_PASSWORD = "auth_password";
    private static final String AUTH_IS_GUEST = "auth_is_guest";
    private static final String AUTH_ACCEPTED_TERMS = "auth_accepted_terms";

    private SharedPreferences sharedPreferences;
    private AuthToken authToken = null;

    @Inject
    public PreferencesStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void save(SettingsPreferences settingsPreferences) {
        sharedPreferences
                .edit()
                .putString(BILL_EMAIL_KEY, settingsPreferences.getEmail())
                .putString(BILL_FIRST_NAME_KEY, settingsPreferences.getFirstName())
                .putString(BILL_LAST_NAME_KEY, settingsPreferences.getLastName())
                .putString(BILL_ADDRESS_KEY, settingsPreferences.getFullAddress())
                .putBoolean(BILL_TO_CARDHOLDER_KEY, settingsPreferences.isBillToCardHolder())
                .putBoolean(READY_SOUND_ON, settingsPreferences.isReadySoundOn())
                .putBoolean(STATE_CHANGE_SOUND_ON, settingsPreferences.isStateChangeSoundOn())
                .putString(PAYMENT_OPTIONS_JSON_KEY, listToJsonString(settingsPreferences.getPaymentOptions()))
                .putString(CARDS_JSON_KEY, listToJsonString(settingsPreferences.getCards()))
                .apply();
        //The editor.apply() method is asynchronous, while editor.commit() is synchronous.

    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public SettingsPreferences getSettingsPreferences() {
        SettingsPreferences settingsPreferences = new SettingsPreferences();
        settingsPreferences.setEmail(sharedPreferences.getString(BILL_EMAIL_KEY, null));
        settingsPreferences.setBillToCardHolder(sharedPreferences.getBoolean(BILL_TO_CARDHOLDER_KEY, false));
        settingsPreferences.setFirstName(sharedPreferences.getString(BILL_FIRST_NAME_KEY, null));
        settingsPreferences.setLastName(sharedPreferences.getString(BILL_LAST_NAME_KEY, null));
        settingsPreferences.setFullAddress(sharedPreferences.getString(BILL_ADDRESS_KEY, null));
        settingsPreferences.setReadySoundOn(sharedPreferences.getBoolean(READY_SOUND_ON, true));
        settingsPreferences.setStateChangeSoundOn(sharedPreferences.getBoolean(STATE_CHANGE_SOUND_ON, true));
        settingsPreferences.setPaymentOptions(getPaymentOptions());
        settingsPreferences.setCards(getCards());
        return settingsPreferences;
    }

    private List<PaymentOption> getPaymentOptions() {
        String paymentOptionsJsonString = sharedPreferences.getString(PAYMENT_OPTIONS_JSON_KEY, "[]");
        Type listType = new TypeToken<ArrayList<PaymentOption>>(){}.getType();
        return new Gson().fromJson(paymentOptionsJsonString, listType);
    }

    private List<CreditCardInfo> getCards() {
        String paymentOptionsJsonString = sharedPreferences.getString(CARDS_JSON_KEY, "[]");
        Type listType = new TypeToken<ArrayList<CreditCardInfo>>(){}.getType();
        return new Gson().fromJson(paymentOptionsJsonString, listType);
    }

    private <T> String listToJsonString(List<T> paymentOptions) {
        return new Gson().toJson(paymentOptions);
    }

    public AuthToken getAuthToken() {
        if (authToken == null) {
            authToken = new AuthToken(
                    sharedPreferences.getString(AUTH_TOKEN, null),
                    sharedPreferences.getString(AUTH_REFRESH_TOKEN, null),
                    sharedPreferences.getString(AUTH_USERNAME, null),
                    sharedPreferences.getString(AUTH_PASSWORD, null),
                    sharedPreferences.getBoolean(AUTH_IS_GUEST, true));
        }
        return authToken;
    }

    public void save(AuthToken authToken) {
        this.authToken.set(authToken);
        sharedPreferences
                .edit()
                .putString(AUTH_TOKEN, authToken.getToken())
                .putString(AUTH_REFRESH_TOKEN, authToken.getRefreshToken())
                .putString(AUTH_USERNAME, authToken.getUsername())
                .putString(AUTH_PASSWORD, authToken.getPassword())
                .putBoolean(AUTH_IS_GUEST, authToken.isGuest())
                .apply();
    }

    public boolean isTermsAccepted() {
        return sharedPreferences.getBoolean(AUTH_ACCEPTED_TERMS, false);
    }

    public void setTermsAccepted(boolean accepted) {
        sharedPreferences
                .edit()
                .putBoolean(AUTH_ACCEPTED_TERMS, accepted).apply();
    }
}
