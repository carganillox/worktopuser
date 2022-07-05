/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.drinklink.app.model.Credentials;
import org.drinklink.app.model.SignUpCredentials;
import org.drinklink.app.model.Token;
import org.drinklink.app.notifications.NotificationsTokenUpdateService;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.utils.Logger;

import java.io.IOException;
import java.util.UUID;

import lombok.Setter;
import retrofit2.Response;

/**
 */
public class AutomaticSignUpInterceptorToken extends InterceptorToken {

    @Setter
    private ApiService notificationsService;
    @Setter
    private NotificationsTokenUpdateService notificationsTokenUpdateService;

    public AutomaticSignUpInterceptorToken(ApiAuthService service, PreferencesStorage preferencesStorage, Context context) {
        super(service, preferencesStorage, context);
    }

    @Override
    protected void refreshToken(int numberOfRetries) {
        if (numberOfRetries == 1) {
            try {
                String random = UUID.randomUUID().toString().replace("-","");
                String username = "drinklink.dev1_" + random;
                String email = "drinklink.dev1+" + random + "@gmail.com";
                String pwd = "Test.123";

                Response<Void> singUp = service.signUp(new SignUpCredentials(username, pwd, email, pwd)).execute();
                Response<Token> tokenResponse = service.authenticate(new Credentials(username, pwd)).execute();


                if (tokenResponse.errorBody() == null) {
                    authToken.setUsername(username);
                    authToken.setToken(tokenResponse.body().getToken());
                    authToken.setRefreshToken(tokenResponse.body().getRefreshToken());
                    authToken.setGuest(true);
                    preferencesStorage.save(authToken);
                    Logger.i(TAG, "refresh token success");
                    Response<String> notifToken = notificationsService.sendNotificationsToken(notificationsTokenUpdateService.getNotificationsToken()).execute();
                    Logger.i(TAG, "refresh token success: " + notifToken.code());
                } else {
                    logout("logout because of code" );
                }
            } catch (IOException e) {
                Logger.e(TAG, "refresh IO exception: " + e.getMessage(), e);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.e(TAG, e.getMessage());
                refreshToken(--numberOfRetries);
            }
        } else {
            super.refreshToken(numberOfRetries);
        }
    }
}
