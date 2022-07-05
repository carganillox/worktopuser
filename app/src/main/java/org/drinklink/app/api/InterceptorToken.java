/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.drinklink.app.model.Token;
import org.drinklink.app.notifications.NotificationsTokenUpdateService;
import org.drinklink.app.persistence.AuthToken;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.ui.activities.SignInActivity;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 */
@Singleton
public class InterceptorToken {

    public static String TAG = "InterceptorToken";

    private final int UNAUTHORIZED_OAUTH2_EXPIRED_TOKEN = 40151;
    private final int UNAUTHORIZED_OAUTH2_INVALID_TOKEN = 40155;
    private final int UNAUTHORIZED_OAUTH2_INVALID_REFRESH_TOKEN_FALLBACK = 400;
    private final int TOO_MANY_REQUESTS = 42000;
    protected final ApiAuthService service;
    protected final PreferencesStorage preferencesStorage;

    public static Object refreshLock = new Object();
    protected final AuthToken authToken;
    private final Context context;

    @Inject
    public InterceptorToken(ApiAuthService service, PreferencesStorage preferencesStorage, Context context) {
        this.service = service;
        this.preferencesStorage = preferencesStorage;
        this.authToken = preferencesStorage.getAuthToken();
        this.context = context;
    }

    public boolean isValid() {
        return hasToken();
    }

    private boolean hasToken() {
        return !TextUtils.isEmpty(getAuthToken());
    }

    private String getAuthToken() {
        return authToken.getToken();
    }

    public String getValidAuthorization() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            refreshToken(null);
        }
        return getAuthorization();
    }

    public String getAuthorization() {
        return String.format("Bearer %s", getAuthToken());
    }

    public synchronized void refreshToken(String currentRequestToken) {
        Logger.i(TAG, "refresh token");
        // optimistic check, check before sync, and after sync as well
        if (currentRequestToken == null) {
            refreshToken(2);
            return;
        }
        if (isValid() && equal(currentRequestToken, getAuthorization())) {
            String currentToken = getAuthToken();
            synchronized (refreshLock) {
                if (equal(currentToken, getAuthToken()) && equal(currentRequestToken, getAuthorization())) {
                    refreshToken(2);
                }
                // else token is already refreshed
            }
        }
    }

    private boolean equal(String s1, String s2) {
        return s1 != null && s1.equals(s2);
    }

    protected void refreshToken(int numberOfRetries) {

        if (numberOfRetries > 0) {
            try {
                Token token = new Token(authToken.getToken(), authToken.getRefreshToken());
                retrofit2.Response<Token> tokenResponse = service.refresh(token).execute();

                if (tokenResponse.errorBody() == null) {
                    authToken.setToken(tokenResponse.body().getToken());
                    authToken.setRefreshToken(tokenResponse.body().getRefreshToken());
                    preferencesStorage.save(authToken);
                    Logger.i(TAG, "refresh token success");
                } else {
                    long code = getResponseCode(tokenResponse);
                    //logCrashlytics(tokenResponse, code);
                    if (shouldLogout(code)) {
                        Logger.w(TAG, "logout directed by server response : " + code);
                        logout("logout because of code");
                    } else if (TOO_MANY_REQUESTS == code) {
                        Logger.i(TAG, "refresh token retry");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                        }
                        refreshToken(numberOfRetries);
                    } else {
                        refreshToken(--numberOfRetries);
                    }
                }
            } catch (IOException e) {
                Logger.e(TAG, "refresh IO exception: " + e.getMessage(), e);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.e(TAG, e.getMessage());
                refreshToken(--numberOfRetries);
            }
        } else {
            Logger.w(TAG, "logout after number of retries");
            logout("number of tries exceeded");
        }
    }

    private boolean shouldLogout(long code) {
        return code == UNAUTHORIZED_OAUTH2_EXPIRED_TOKEN
                || code == UNAUTHORIZED_OAUTH2_INVALID_TOKEN
                || code == UNAUTHORIZED_OAUTH2_INVALID_REFRESH_TOKEN_FALLBACK;
    }

    private long getResponseCode(Response<Token> tokenResponse) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(tokenResponse.errorBody().string());
        return jsonObject.getLong("code");
    }

    public void logout(String message) {
        Logger.i(TAG, "logout:" + message);
        if (isValid()) {
            authToken.setToken(null);
            authToken.setRefreshToken(null);
            preferencesStorage.save(authToken);
//            CrashlyticsLog.log(new AuthTokenRefreshException("WARNING : logout on refresh token, " + message));
//            LoginUtils.disconnect(context);
            Intent intent = new Intent(context, SignInActivity.class);
            intent.setFlags(IntentUtils.NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void setNotificationsService(ApiService apiService) {

    }

    public void setNotificationsTokenUpdateService(NotificationsTokenUpdateService notificationsTokenUpdateService) {

    }

//    public void setToken(String authToken, String csrfToken) {
//        this.authToken = authToken;
//        this.csrfToken = csrfToken;
//        this.refreshToken = null;
//    }

//    public String getToken() {
//        return authToken;
//    }

//    public String getCsrfHeader() {
//        return csrfToken != null ? csrfToken : "";
//    }
}
