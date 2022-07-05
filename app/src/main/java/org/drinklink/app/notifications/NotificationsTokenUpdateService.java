/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.notifications;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiAuthService;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.utils.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import lombok.Getter;
import retrofit2.Call;

/**
 *
 */
@Singleton
public class NotificationsTokenUpdateService {

    private static final String TAG = "NotificationsTokenUpdateService";

    private static final String CHANNEL_ID = "DrinkLinkNotificationsChannel";

    @Getter
    private String notificationsToken;

    PreferencesStorage preferencesStorage;

    ApiAuthService apiAuthService;

    ApiService apiService;

    Context context;

    @Inject
    public NotificationsTokenUpdateService(PreferencesStorage preferencesStorage,
                                           ApiAuthService apiAuthService,
                                           @Named(ApplicationModule.API_SERVICE)
                                           ApiService apiService,
                                           Context context) {
        this.preferencesStorage = preferencesStorage;
        this.apiAuthService = apiAuthService;
        this.apiService = apiService;
        this.context = context;
    }

    public void subscribe() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Logger.e(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult();

                    // Log and toast
                    String msg = context.getString(R.string.msg_token_fmt, token);
                    notificationsToken = token;
                    Logger.d(TAG, msg);
                    sendRegistrationToServer(token);
                });
    }

    private void sendRegistrationToServer(String token) {

        String username = preferencesStorage.getAuthToken().getUrlUsername();
        if (TextUtils.isEmpty(username)) {
            Logger.i(TAG, "no user name");
            return;
        }

        Call<String> authenticate = apiService.sendNotificationsToken(token);
        authenticate.enqueue(new ActionCallback<String>(new ProgressBarCounter(null), null) {
            @Override
            public void onSuccess(String body) {
                Logger.i(TAG, "upload token success " + body);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                Logger.i(TAG, "upload notifications token: " + message);
                FirebaseCrashlytics.getInstance().log("Notifications token upload failed");
            }
        });
    }
}
