/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.workflow;

import androidx.annotation.NonNull;

import org.drinklink.app.common.viewholder.IItemUpdated;
import org.drinklink.app.common.viewholder.UpdateFunction;
import org.drinklink.app.common.viewholder.UpdateSingleFunction;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.Discount;
import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.Tip;
import org.drinklink.app.model.request.DrinkRequest;
import org.drinklink.app.model.request.MixerRequest;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.model.request.OrderResponse;
import org.drinklink.app.persistence.DataStorage;
import org.drinklink.app.persistence.model.DrinkSelection;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.ListenersTracker;
import org.drinklink.app.utils.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import rx.observables.BlockingObservable;

import static org.drinklink.app.utils.MoneyUtils.greaterThanZero;
import static org.drinklink.app.utils.MoneyUtils.round;

/**
 *
 */

public class OrderProcessor implements IOrderProcessor {

    private static final String TAG = "OrderProcessor";

    private ListenersTracker<IItemUpdated, OrderItemRequest> listeners = new ListenersTracker<IItemUpdated, OrderItemRequest>() {
        @Override
        protected void call(IItemUpdated listener) {
            listener.onUpdated();
        }
    };

    private static final UpdateFunction<IItemUpdated, OrderItemRequest, Void> REMOVE_FUNCTION =
            (iItemUpdated, itemSelection) -> {
                iItemUpdated.itemRemoved(itemSelection);
                return null;
            };

    private static final UpdateFunction<IItemUpdated, OrderItemRequest, Void> ADD_FUNCTION =
            (iItemUpdated, itemSelection) -> {
                iItemUpdated.itemAdded(itemSelection);
                return null;
            };

    private static final UpdateSingleFunction<IItemUpdated, Void> UPDATE_FUNCTION =
            iItemUpdated -> {
                iItemUpdated.onUpdated();
                return null;
            };

    private static final UpdateSingleFunction<IItemUpdated, Void> MERGED_FUNCTION =
            iItemUpdated -> {
                iItemUpdated.onMerged();
                return null;
            };

    private DataStorage dataStorage;

    private OrderPreparation orderPreparation;
    private OrderPreparation lastOrder;

    private List<OrderPreparation> pendingOrders = new CopyOnWriteArrayList<>();
    private List<OrderUpdateListener> orderUpdateListeners = new CopyOnWriteArrayList<>();

    //private static final HashMap<Integer, Place> places = new HashMap<>();


    public OrderProcessor() {
        this.orderPreparation = new OrderPreparation();
        setLastOrder(null);
        Logger.i(TAG, "Temp order processor created");
    }

    public OrderProcessor(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        dataStorage.deleteOrderPreparation(OrderPreparation.NOT_PUBLISHED_ORDER_ID);
        List<OrderPreparation> orders = BlockingObservable.from(dataStorage.getOrderPreparations()).single();
        pendingOrders = new CopyOnWriteArrayList<>(orders);
        this.orderPreparation = new OrderPreparation();
        OrderPreparation first = ListUtil.findFirst(pendingOrders, item -> item.isActive());
        setLastOrder(first);
        Logger.i(TAG, "OrderProcessor initialized");
    }

    private void setLastOrder(OrderPreparation first) {
        this.lastOrder = first != null ? first : this.orderPreparation;
    }

    public OrderProcessor(OrderPreparation orderPreparation) {
        Logger.i(TAG, "Order processor for a preparation created " + orderPreparation.getId());
        this.orderPreparation = orderPreparation;
        setLastOrder(null);
        pendingOrders = new ArrayList<>();
        pendingOrders.add(orderPreparation);
    }

    private OrderPreparation getOrderPreparation() {
        return orderPreparation;
    }

    @Override
    public Map<OrderKey, OrderItemRequest> getDrinkSelection() {
        return orderPreparation.getDrinkSelection();
    }

    @Override
    public void setOrder(Order order) {
        this.orderPreparation.setOrder(order);
        save();
    }

    @Override
    public void setDiscount(Discount discount) {
        orderPreparation.setDiscount(discount);
    }

    @Override
    public Discount getDiscount() {
        return orderPreparation.getDiscount();
    }

    @Override
    public void setTip(Tip tip) {
        this.orderPreparation.setTip(tip);
    }

    @Override
    public Order getOrder() {
        return orderPreparation.getOrder();
    }

    @Override
    public void setBar(Bar bar) {
        orderPreparation.setBar(bar);
    }

    @Override
    public void reset() {
        Place place = getPlace();
        orderPreparation = new OrderPreparation();
        orderPreparation.setPlace(place);
//        save();

        notifyListenersMerged();
        notifyListenersUpdated();
    }

    @Override
    public void forPlace(Place place) {
        if (place != null && this.getPlace().getId() != place.getId()) {
            reset();
        }
        orderPreparation.setPlace(place);
        place.setUserSelected(true);
//        save();
//        places.put(place.getId(), place);
    }

    @Override
    public Place getPlace() {
        Place place = orderPreparation.getPlace();
        return placeOrDefault(place);
    }

    @NotNull
    private Place placeOrDefault(Place place) {
        return place != null ? place : Place.EMPTY_PLACE;
    }

//    @Deprecated
//    public OrderItemRequest getDrinkCount(Drink drink) {
//        for (Map.Entry<OrderKey, OrderItemRequest> entry : drinkSelection.entrySet()) {
//            if (entry.getKey().getDrinkId() == drink.getId()) {
//                return entry.getValue();
//            }
//        }
//        return null;
//    }

    public <T extends NamedObject> void remove(Drink drink, boolean withIce, List<T> mixers) {

        OrderKey orderKey = new OrderKey(drink.getId(), getMixerIds(mixers), withIce);
        OrderItemRequest item = getDrinkSelection().get(orderKey);

        getDrinkSelection().remove(orderKey);
        notifyListenersRemoved(item);

        notifyListenersUpdated();
//        save();
    }

    @NonNull
    private OrderItemRequest putOrderItem(Drink drink, boolean withIce, List<DrinkOption> mixers, OrderKey orderKey) {
        OrderItemRequest orderItemRequest = prepareOrderItemRequest(drink, withIce, mixers);
        getDrinkSelection().put(orderKey, orderItemRequest);
        return orderItemRequest;
    }

    @NonNull
    private OrderItemRequest prepareOrderItemRequest(Drink drink, boolean withIce, List<DrinkOption> mixers) {
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setDrink(new DrinkRequest(drink));
        orderItemRequest.setQuantity(0);
        orderItemRequest.setWithIce(withIce);
        List<MixerRequest> selectedMixer = ListUtil.transform(mixers, mixer -> new MixerRequest(mixer));
        orderItemRequest.setSelectedMixers(selectedMixer);
        orderItemRequest.setOriginalDrink(drink);
        return orderItemRequest;
    }

    @Override
    public void addDrink(Drink drink, boolean withIce, List<DrinkOption> mixers, int count) {
        OrderKey newOrderKey = new OrderKey(drink.getId(), getMixerIds(mixers), withIce);
        OrderItemRequest newItem = putOrderItem(drink, withIce, mixers, newOrderKey);
        newItem.setQuantity(count);
        getDrinkSelection().put(newOrderKey, newItem);
    }

    private <T extends NamedObject> List<Integer> getMixerIds(List<T> mixer) {
        return mixer != null ? ListUtil.transform(mixer, item -> item.getId()) : null;
    }

    @Override
    public void notifyUpdate() {
        notifyListenersUpdated();
//        save();
    }

    private void increment(OrderKey key, OrderItemRequest value) {
        OrderItemRequest orderItemRequest = getDrinkSelection().get(key);
        if (orderItemRequest == null) {
            getDrinkSelection().put(key, value);
        } else {
            int newCount = orderItemRequest.getQuantity() + value.getQuantity();
            orderItemRequest.setQuantity(newCount);
        }
    }

    @Override
    public BigDecimal sumPlainDrinkWithMixerPrice() {
        return orderPreparation.sumPlainDrinkWithMixerPrice();
    }

    @Override
    public void registerListener(IItemUpdated<OrderItemRequest> listener) {
        listeners.registerListener(listener);
    }

    @Override
    public void unRegisterListener(IItemUpdated<OrderItemRequest> listener) {
        listeners.unRegisterListener(listener);
    }

    private void notifyListenersUpdated() {
        listeners.notifyListeners(UPDATE_FUNCTION);
    }

    private void notifyListenersMerged() {
        listeners.notifyListeners(MERGED_FUNCTION);
    }

    private void notifyListenersAdded(OrderItemRequest item) {
        listeners.notifyListeners(item, ADD_FUNCTION);
    }

    private void notifyListenersRemoved(OrderItemRequest item) {
        listeners.notifyListeners(item, REMOVE_FUNCTION);
    }

    @Override
    public boolean isEmpty() {
        return getDrinkSelection().isEmpty();
    }

    @Override
    public int getCount() {
        return orderPreparation.getCount();
    }

    @Override
    public List<OrderItemRequest> getOrderItems() {
        return orderPreparation.getOrderItems();
    }

    @Override
    public boolean merge(IOrderProcessorPreview currentProcessor) {
        for (Map.Entry<OrderKey, OrderItemRequest> entry : currentProcessor.getDrinkSelection().entrySet()) {
            if (entry.getValue().getQuantity() > 0) {
                increment(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<OrderKey, OrderItemRequest> entry : new ArrayList<>(getDrinkSelection().entrySet())) {
            if (entry.getValue().getQuantity() <= 0) {
                getDrinkSelection().remove(entry.getKey());
            }
        }
        updateList();
        notifyListenersMerged();
//        save();
        return true;
    }

    @Override
    public void updateList() {
        orderPreparation.updateList();
    }

    @Override
    public void next(CreditCardInfo newCard) {
        OrderPreparation currentOrderPreparation = getOrderPreparation();
        currentOrderPreparation.setSavedCard(newCard);
        currentOrderPreparation.setCreated(System.currentTimeMillis());
        pendingOrders.add(0, currentOrderPreparation);
        reset();
        lastOrder = currentOrderPreparation;
    }

    @Override
    public void storeOrder(Order order) {
        order.captureLastModified();
        getOrderPreparation().setOrder(order);
    }

    public void save() {
        dataStorage.addOrUpdateOrderPreparation(getOrderPreparation(), pendingOrders);
    }

    @Override
    public OrderRequest asOrderRequest(Integer barId, Integer tableId) {

        OrderRequest order = new OrderRequest();
        order.setFacilityId(getPlace().getId());
        order.setBarId(barId);
        order.setTableId(tableId);

        for (Map.Entry<OrderKey, OrderItemRequest> entry : getDrinkSelection().entrySet()) {
            OrderItemRequest orderItem = entry.getValue();
            if (orderItem.getQuantity() <= 0) {
                continue;
            }
            orderItem.setPrice(orderItem.getRoundedTotalItemPrice());
            order.getItems().add(orderItem);
        }
        order.setTip(getTipValue());
        order.setOriginalPrice(sumPlainDrinkWithMixerPrice());
        order.setDiscountId(getDiscountObject() != null ? getDiscountObject().getId() : null);
        order.setFinalPrice(getTotal());
        order.setVipCharge(getVipCharge());
        order.setServiceCharge(getServiceChargeAbsolute());
        order.setVip(greaterThanZero(getVipCharge()));
        return order;
    }

    private Discount getDiscountObject() {
        return orderPreparation.getDiscount();
    }


    @Override
    public BigDecimal getDiscountValue() {
        return orderPreparation.getDiscountValue();
    }

    @Override
    public BigDecimal getTipValue() {
        return orderPreparation.getTipValue();
    }

    @Override
    public BigDecimal getTotal() {
        return orderPreparation.getTotal();
    }

    @Override
    public BigDecimal getVipCharge() {
        return orderPreparation.getVipCharge();
    }

    @Override
    public void setVipCharge(BigDecimal vip) {
        orderPreparation.setVipCharge(vip);
    }

    @Override
    public BigDecimal getServiceChargeAbsolute() {
        return round(orderPreparation.getTotal().subtract(orderPreparation.getTotalBeforeServiceChange()));
    }

    @Override
    public void setServiceChargePercentage(BigDecimal serviceCharge) {
        orderPreparation.setServiceCharge(serviceCharge);
    }

    @Override
    public Bar getBar() {
        Order order = getOrder();
        if (order == null) {
            return null;
        }
        if (order.getBar() != null) {
            return order.getBar();
        }
        return getPlace().getBar(order.getBarId());
    }

    @Override
    public void addOrderUpdateListener(OrderUpdateListener orderUpdateListener) {
        this.orderUpdateListeners.add(orderUpdateListener);
    }

    @Override
    public void removeOrderUpdateListener(OrderUpdateListener orderUpdateListener) {
        this.orderUpdateListeners.remove(orderUpdateListener);
    }

    /**
     * Return if orther change is handled
     * @return
     */
    @Override
    public boolean updateOrder(Order order, OrderStates previousOrderState) {
        return notifyOrderUpdateListeners(order, previousOrderState);
    }

    /**
     * @return previosu state of the order
     */
    public OrderStates updateCurrentOrder(Order order, OrderPreparation matchingOrderPreparation) {
        Order currentOrder = matchingOrderPreparation.getOrder();
        order.update(currentOrder);
        OrderStates currentOrderState = currentOrder.getCurrentOrderState();
        matchingOrderPreparation.setOrder(order);
        order.captureLastModified();
        save();
        return currentOrderState;
    }

    public OrderPreparation getMatchingOrderPreparation(int orderId) {
        OrderPreparation matchingOrderPreparation = ListUtil.findFirst(pendingOrders, item -> orderId == item.getId());
        if (matchingOrderPreparation != null) {
            Logger.i(TAG, "getMatchingOrderPreparation, found match for: " + orderId);
        } else {
            Logger.i(TAG, "getMatchingOrderPreparation, NOT found match for: " + orderId + ", existing: " + getAllIds());
        }
        return matchingOrderPreparation;
    }

    private String getAllIds() {
        String ids = "";
        for (OrderPreparation  preparations : pendingOrders) {
            ids = ids + preparations.getId() + ",";
        }
        return ids;
    }

    @Override
    public Boolean alertOrder(int orderId, boolean isBarOrder) {
        return notifyAlertListeners(orderId, isBarOrder);
    }

    public IOrderProcessorPreview findOrderProcessor(int orderId) {
        return findOrderProcessorFull(orderId);
    }

    private OrderPreparation findOrderProcessorFull(int orderId) {
        OrderPreparation first = ListUtil.findFirst(pendingOrders, item -> {
            int currentOrderId = item.getId();
            Logger.i(TAG, "check order: " + currentOrderId);
            return currentOrderId == orderId;
        });
        return first;
    }

    @Override
    public void tryTimeout() {
        Order order = orderPreparation.getOrder();
        if (order != null && System.currentTimeMillis() - order.getLastModified() > TimeUnit.HOURS.toMillis(1)) {
            reset();
        }
    }

    private boolean notifyOrderUpdateListeners(Order order, OrderStates previousOrderState) {
        boolean isHandled = false;
        for (OrderUpdateListener listener : orderUpdateListeners) {
            if (listener.isMatch(order.getId())) {
                isHandled |= listener.onOrderUpdated(order, previousOrderState);
            }
        }
        return isHandled;
    }

    // null indicate that there is no listener so application is not running
    private Boolean notifyAlertListeners(int orderId, boolean isBarOrder) {
        if (orderUpdateListeners.isEmpty()) {
            return null;
        }
        boolean isAlerted = false;
        for (OrderUpdateListener listener : orderUpdateListeners) {
            // notify only listeners to specific orderId
            if (listener.isMatch(orderId)) {
                isAlerted |= listener.onOrderAlert(orderId, isBarOrder);
            }
        }
        return isAlerted;
    }

    @Override
    public int getId() {
        return orderPreparation.getId();
    }

    @Override
    public boolean isActive() {
        return lastOrder.isActive();
    }

    @Override
    public int getNumberOfActiveOrders() {
        return ListUtil.select(pendingOrders, item -> item.isActive()).size();
    }

    @Override
    public int getActiveOrderId() {
        return lastOrder.getId();
    }

    @Override
    public Place getActiveOrderPlace() {
        return placeOrDefault(lastOrder.getPlace());
    }

    @Override
    public void setLastActive(int requestedOrderId) {
        lastOrder = findOrderProcessorFull(requestedOrderId);
    }

    @Override
    public boolean isPaymentSuccess() {
        return orderPreparation.isPaymentSuccess();
    }

    @Override
    public long getCreated() {
        return orderPreparation.getCreated();
    }

    @Override
    public List<IOrderProcessorPreview> getOrderPreviews() {
        return new ArrayList<>(pendingOrders);
    }

    @Override
    public void recoverOrder(OrderResponse order) {
        OrderPreparation prep = new OrderPreparation();
        prep.setOrder(order);
        prep.setPlace(new Place());

        ArrayList<DrinkSelection> drinkSelectionList = ListUtil.transform(order.getItems(),
                item -> new DrinkSelection(asKey(item), asRequest(item)));
        prep.setDrinkSelectionList(drinkSelectionList);

        prep.updateSelection();
        order.captureLastModified();
        pendingOrders.add(prep);
        save();

    }

    @Override
    public void deleteOrderPreparation(int id) {
        OrderPreparation matchingOrderPreparation = getMatchingOrderPreparation(id);
        pendingOrders.remove(matchingOrderPreparation);
        dataStorage.deleteOrderPreparation(id);
    }

    private OrderKey asKey(OrderItemRequest item) {
        OrderKey orderKey = new OrderKey(item.getDrink().getId(), getMixerIds(item.getSelectedMixers()), item.isWithIce());
        return orderKey;
    }

    private OrderItemRequest asRequest(OrderItemRequest item) {
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setDrink(item.getDrink());
        orderItemRequest.setQuantity(0);
        orderItemRequest.setWithIce(item.isWithIce());
        orderItemRequest.setSelectedMixers(item.getSelectedMixers());
        return orderItemRequest;
    }
}
