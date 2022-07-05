/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import org.drinklink.app.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

    private static final String TAG = "AuthToken";

    private String token;
    private String refreshToken;
    private String username;
    private String password;
    private boolean isGuest;

    public AuthToken(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public boolean isSignedIn() {
        return token != null && !token.isEmpty() && !isGuest;
    }

    public void set(AuthToken authToken) {
        token = authToken.token;
        refreshToken = authToken.refreshToken;
        username = authToken.username;
        password = authToken.password;
        isGuest = authToken.isGuest;
    }

    public String getUrlUsername() {
        try {
            return username != null ? URLEncoder.encode( username, "UTF-8" ) : null;
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, e.getMessage());
            return username;
        }
    }
}
