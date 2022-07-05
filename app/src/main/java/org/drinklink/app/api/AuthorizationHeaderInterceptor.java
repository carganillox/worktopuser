/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import org.drinklink.app.utils.Logger;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import okio.Buffer;
import okio.BufferedSource;

/**

 */
public class AuthorizationHeaderInterceptor implements Interceptor, Authenticator {

    public static final String TAG = "AuthorizationHeaderInterceptor";

    public final int CANCELED_CODE = 404; // this could be any other error code as well
    public final int REQUEST_TMEOUT = 408;

    public static final String AUTHORIZATION_HEADER = "Authorization";
//    public static final String CSRF_HEADER = "x-csrf-token";

    private final InterceptorToken token;

    public AuthorizationHeaderInterceptor(InterceptorToken token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Logger.i(TAG, "intercept request:" + chain.call().request().url());
        Request request = chain.request();
        Request authorizedRequest = authorizeRequest(request);
        Response response = chain.proceed(authorizedRequest);
        Logger.i(TAG, "request finished: " + chain.call().request().url());
        return response;
    }

    public static Response getCustomResponse(Chain chain, int code, String message) {
        Response response = new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code) //There is no code for that. I've subjectively put this one
                .message(message)
                .body(new ResponseBody() {
                    @Override public MediaType contentType() {
                        return null;
                    }

                    @Override public long contentLength() {
                        return 0;
                    }

                    @Override public BufferedSource source() {
                        return new Buffer();
                    }
                })
                .build();
        return response;
    }

    private Request authorizeRequest(Request request) {
        if (!token.isValid()) {
            return request;
        }
        return request.newBuilder()
                .header(AUTHORIZATION_HEADER, token.getAuthorization())
//                .header(CSRF_HEADER, token.getCsrfHeader())
                .build();
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Refresh your access_token using a synchronous api request
        String headerAuthorization = response.request().header(AUTHORIZATION_HEADER);
        token.refreshToken(headerAuthorization);
        // Add new header to rejected request and retry it or null if authorization fails
        return token.isValid() ? authorizeRequest(response.request()) : null;
    }
}
