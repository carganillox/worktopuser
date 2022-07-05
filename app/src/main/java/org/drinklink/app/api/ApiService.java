/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import org.drinklink.app.model.AuthorizationResponse;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.OrderCancellation;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.request.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.Single;

/**
 *
 */

public interface ApiService {

    @GET("places")
    Observable<List<Place>> getPlaces(@Query("page") int pageId);

    @GET("places/{id}")
    Observable<Place> getMenu(@Path("id") long menuId);

    @POST("orders")
    Call<Order> postOrder(@Body OrderRequest order);

    @POST("orders")
    Single<Order> postOrderRx(@Body OrderRequest order);

    @POST("update-transaction")
    Call<Order> updateTransaction(long orderId, String transactionId);

    @GET("orders/{order_id}")
    Call<OrderResponse> getOrder(@Path("order_id") long orderId);

    @PATCH("orders/{order_id}")
    Call<Order> cancel(@Path("order_id") long orderId, @Body OrderCancellation orderCancellation);

    @PUT("payment/savecard")
    Single<AuthorizationResponse> authorizeCard();

    @PATCH("auth/users/currentUser/notificationToken")
    Call<String> sendNotificationsToken(@Body String token);

    @GET("users/currentUser/savedcards")
    Call<List<CreditCardInfo>> getSavedCreditCards();

    @DELETE("users/currentUser/savedcards")
    Call<Void> deleteSavedCards();

    @POST("users/currentUser/savedcards")
    Call<AuthorizationResponse> addSavedCard();


    @GET("users/currentUser/orders")
    Call<List<CreditCardInfo>> getAllOrders();

    void reset();
}
