/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.workflow;

import org.drinklink.app.common.viewholder.IItemUpdated;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.Discount;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.Tip;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.model.request.OrderResponse;
import org.drinklink.app.persistence.model.OrderPreparation;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 */

public interface IOrderProcessor extends IOrderProcessorPreview {

    static final int MAX_DRINK_COUNT = 99;

    void addDrink(Drink drink, boolean withIce, List<DrinkOption> mixers, int count);

    void notifyUpdate();

    <T extends NamedObject> void remove(Drink drink, boolean withIce, List<T> mixers);

    void reset();

    void forPlace(Place place);

    void registerListener(IItemUpdated<OrderItemRequest> listener);

    void unRegisterListener(IItemUpdated<OrderItemRequest> listener);

    OrderRequest asOrderRequest(Integer barId, Integer tableId);

    boolean isEmpty();

    void setOrder(Order order);

    boolean merge(IOrderProcessorPreview currentProcessor);

    void next(CreditCardInfo newCard);

    void storeOrder(Order order);

    void setDiscount(Discount discount);

    Discount getDiscount();

    void setTip(Tip tip);

    void save();

    BigDecimal getVipCharge();

    void setVipCharge(BigDecimal vip);

    BigDecimal getServiceChargeAbsolute();

    void setServiceChargePercentage(BigDecimal serviceCharge);

    void addOrderUpdateListener(OrderUpdateListener orderUpdateListener);

    void removeOrderUpdateListener(OrderUpdateListener orderUpdateListener);

    boolean updateOrder(Order order, OrderStates previousOrderState);

    OrderPreparation getMatchingOrderPreparation(int orderId);

    OrderStates updateCurrentOrder(Order order, OrderPreparation matchingOrderPreparation);

    Boolean alertOrder(int orderId, boolean isBarOrder);

    IOrderProcessorPreview findOrderProcessor(int orderId);

    void tryTimeout();

    void updateList();

    List<IOrderProcessorPreview> getOrderPreviews();

    void recoverOrder(OrderResponse order);

    void deleteOrderPreparation(int id);

    int getActiveOrderId();

    Place getActiveOrderPlace();

    void setLastActive(int requestedOrderId);

    interface OrderUpdateListener {
        boolean onOrderUpdated(Order order, OrderStates previousState);

        boolean isMatch(int orderId);

        boolean onOrderAlert(int orderId, boolean isBarOrder);
    }

    abstract class OrderUpdateListenerAdapter implements OrderUpdateListener {

        @Override
        public boolean isMatch(int orderId) {
            return true;
        }

        @Override
        public boolean onOrderAlert(int orderId, boolean isBarOrder) {
            return false;
        }
    }
}
