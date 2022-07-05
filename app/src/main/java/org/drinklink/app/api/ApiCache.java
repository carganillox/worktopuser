/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.api;

import android.util.LruCache;

import org.drinklink.app.model.AuthorizationResponse;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.OrderCancellation;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.request.OrderResponse;
import org.drinklink.app.utils.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Path;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */

public class ApiCache implements ApiService {

    private static final String TAG = "ApiCache";

    private static final long EXPIRATION = TimeUnit.MINUTES.toMillis(15);

    private LruCache<String, Observable<?>> apiObservables = new LruCache<>(10);
//    private LruCache<String, Call<?>> apiCalls = new LruCache<>(10);
    private HashMap<String, Long> observableTimestamp = new HashMap<>();

    private final ApiService apiService;

    public ApiCache(ApiService apiService) {
        this.apiService = apiService;
    }

    public <T> Observable<T> getFromCache(Observable<T> unPreparedObservable, Object ... args){

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String key = stackTraceElements[3].getMethodName() + Arrays.toString(args);

        Observable<T> observable = (Observable<T>)apiObservables.get(key);
        Logger.d(TAG, "get from cache : " + key + ", found: " + (observable != null));

        if (observable == null || isExpired(key)) {
            observable = unPreparedObservable
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();

            apiObservables.put(key, observable);
            observableTimestamp.put(key, now());
        }
        return observable;
    }

    private boolean isExpired(String key) {
        return now() - observableTimestamp.get(key) > EXPIRATION;
    }

    private long now() {
        return System.currentTimeMillis();
    }

    @Override
    public Observable<List<Place>> getPlaces(int page) {
        return getFromCache(apiService.getPlaces(page), page);
    }

    @Override
    public Observable<Place> getMenu(long menuId) {
        return getFromCache(apiService.getMenu(menuId), menuId);
    }

    // actions

    @Override
    public Call<Order> postOrder(@Body OrderRequest order) {
        return null;
    }

    @Override
    public Call<OrderResponse> getOrder(@Path("order_id") long orderId) {
        return null;
    }

    @Override
    public Call<Order> cancel(@Path("order_id") long orderId, @Body OrderCancellation orderCancellation) {
        return apiService.cancel(orderId, orderCancellation);
    }

    @Override
    public Call<Order> updateTransaction(long orderId, String transactionId) {
        return null;
    }

    @Override
    public Call<String> sendNotificationsToken(String token) {
        return null;
    }

    @Override
    public void reset() {
        apiObservables.evictAll();
    }

    @Override
    public Single<AuthorizationResponse> authorizeCard() {
        return null;
    }

    @Override
    public Single<Order> postOrderRx(OrderRequest order) {
        return null;
    }

    @Override
    public Call<List<CreditCardInfo>> getSavedCreditCards() {
        return apiService.getSavedCreditCards();
    }

    @Override
    public Call<Void> deleteSavedCards() {
        return null;
    }

    @Override
    public Call<List<CreditCardInfo>> getAllOrders() {
        return null;
    }

    @Override
    public Call<AuthorizationResponse> addSavedCard() {
        return null;
    }
}
