/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import org.drinklink.app.model.Credentials;
import org.drinklink.app.model.SignUpCredentials;
import org.drinklink.app.model.Token;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 *
 */

public interface ApiAuthService {

    @POST("auth/Token")
    Call<Token> authenticate(@Body Credentials credentials);

    @POST("auth/refresh")
    Call<Token> refresh(@Body Token token);

    @POST("auth/users")
    Call<Void> signUp(@Body SignUpCredentials credentials);
}
