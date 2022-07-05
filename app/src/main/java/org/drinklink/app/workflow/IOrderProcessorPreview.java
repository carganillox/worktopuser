/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.workflow;

import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Discount;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.utils.ListUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 */

public interface IOrderProcessorPreview {

    int getId();

    Order getOrder();

    Place getPlace();

    void setBar(Bar bar);

    Bar getBar();

    BigDecimal getDiscountValue();

    Discount getDiscount();

    List<OrderItemRequest> getOrderItems();

    Map<OrderKey, OrderItemRequest> getDrinkSelection();

    int getCount();

    void addDrink(Drink drink, boolean selected, List<DrinkOption> mixers, int count);

    <T extends NamedObject> void remove(Drink drink, boolean withIce, List<T> mixers);

    void notifyUpdate();

    BigDecimal getTotal();

    BigDecimal getTipValue();

    BigDecimal sumPlainDrinkWithMixerPrice();

    boolean isActive();

    int getNumberOfActiveOrders();

    boolean isPaymentSuccess();

    long getCreated();
}
