package org.drinklink.app.payment;

import android.app.Activity;
import android.content.Intent;

import org.drinklink.app.api.ApiService;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.IAuthorizationResponse;
import org.drinklink.app.model.request.DrinkRequest;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.utils.MoneyUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;

import payment.sdk.android.cardpayment.CardPaymentActivity;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */
public class InternationalGatewayPaymentManager {

    public static final String URL_KEY = "gateway-payment-url";
    public static final String CODE = "code";
    private static final String SALE = "SALE";
    private static final String AUTH = "AUTH";

    public static Single<Intent> authorizePayment(ApiService apiService, Activity activity) {
        OrderRequest order = getOrderRequest();
        return pay(apiService, activity, order);
    }

    @NotNull
    public static OrderRequest getOrderRequest() {
        OrderRequest order = new OrderRequest();
        order.setFacilityId(1);
        BigDecimal finalPrice = new BigDecimal(3.2f);
        order.setFinalPrice(finalPrice);
        order.setOriginalPrice(finalPrice);
        ArrayList<OrderItemRequest> items = new ArrayList<>();
        order.setItems(items);
        OrderItemRequest orderItem = new OrderItemRequest();
        items.add(orderItem);
        Drink drink = new Drink();
        drink.setDrinkCategoryId(2);
        drink.setId(5);
        drink.setPrice(finalPrice);
        drink.setVolume("0.25");
        orderItem.setDrink(new DrinkRequest(drink));
        orderItem.setPrice(finalPrice);
        orderItem.setQuantity(1);
        order.setSaveCardInfo(true);
        return order;
    }

    public static Single<Intent> saveCard(ApiService apiService, Activity activity) {
        Single<Intent> intentSingle = apiService.authorizeCard()
                .map(createResponse -> startPayment(createResponse, activity))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return intentSingle;
    }

    public static Single<Intent> pay(ApiService apiService, Activity activity, OrderRequest order) {

//        Single<Intent> intentSingle = apiService.authorizeCard()
        Single<Intent> intentSingle = apiService.postOrderRx(order)
                .map(createResponse -> startPayment(createResponse, activity))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return intentSingle;
    }

    public static Intent startPayment(IAuthorizationResponse createRequest, Activity activity) {

        Intent intent = new Intent(activity.getApplicationContext(), CardPaymentActivity.class);
        intent.putExtra(URL_KEY, createRequest.getPaymentAuthorizationLink());
        intent.putExtra(CODE, createRequest.getPaymentOrderCode());
        return intent;
    }

}
