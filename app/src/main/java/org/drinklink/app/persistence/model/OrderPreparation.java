/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence.model;

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
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.MoneyUtils;
import org.drinklink.app.utils.SortUtils;
import org.drinklink.app.workflow.IOrderProcessorPreview;
import org.drinklink.app.workflow.OrderKey;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.drinklink.app.utils.MoneyUtils.round;
import static org.drinklink.app.utils.MoneyUtils.toPercentageRounded;

/**
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderPreparation implements IOrderProcessorPreview {

    public static final int NOT_PUBLISHED_ORDER_ID = -1;

    private Place place;
    private transient Map<OrderKey, OrderItemRequest> drinkSelection = new HashMap<>();
    private List<DrinkSelection> drinkSelectionList = new ArrayList<>();
    private Order order;
    private Discount discount;
    private BigDecimal serviceCharge = BigDecimal.ZERO;
    private Tip tip;
    private Bar bar;
    private CreditCardInfo savedCard;
    private BigDecimal vipCharge = BigDecimal.ZERO;
    private boolean paymentSuccess = false;
    private boolean deleted;
    private long created;

    public OrderPreparation(OrderPreparation orderPreparation) {
        this.order = orderPreparation.getOrder();
        this.drinkSelectionList = orderPreparation.drinkSelectionList;
        this.place = orderPreparation.place;
        this.discount = orderPreparation.getDiscount();
        this.tip = orderPreparation.getTip();
        this.vipCharge = orderPreparation.getVipCharge();
        this.serviceCharge = orderPreparation.getServiceCharge();
        this.savedCard = orderPreparation.getSavedCard();
        this.created = orderPreparation.getCreated();
    }

    public void updateList() {
        drinkSelectionList = ListUtil.transform(drinkSelection.entrySet(),
                item -> new DrinkSelection(item.getKey(), item.getValue()));
    }

    public OrderPreparation updateSelection() {
        for (DrinkSelection ds : drinkSelectionList) {
            drinkSelection.put(ds.getOrderKey(), ds.getOrderItemRequest());
        }
        return this;
    }

    public Map<OrderKey, OrderItemRequest> getDrinkSelection() {
        if (drinkSelection.isEmpty() && !drinkSelectionList.isEmpty()) {
            for (DrinkSelection item : drinkSelectionList) {
                drinkSelection.put(item.getOrderKey(), item.getOrderItemRequest());
            }
        }
        return drinkSelection;
    }

    public int getId() {
        return order != null ? order.getId() : NOT_PUBLISHED_ORDER_ID;
    }

    @Override
    public boolean isActive() {
        Order order = getOrder();
        return order != null && !order.isFinished() && !order.isExpired() && !paymentFailed(order);
    }

    private boolean paymentFailed(Order order) {
        return order.getCurrentOrderState() == OrderStates.OrderCreated && !isPaymentSuccess();
    }

    @Override
    public int getNumberOfActiveOrders() {
        return isActive() ? 1 : 0;
    }

    @Override
    public List<OrderItemRequest> getOrderItems() {
        return SortUtils.sort(new ArrayList<>(getDrinkSelection().values()));
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Map.Entry<OrderKey, OrderItemRequest> entry : getDrinkSelection().entrySet()) {
            count += entry.getValue().getQuantity();
        }
        return count;
    }

    @Override
    public BigDecimal getTipValue() {
        return getTip(getTotalAfterDiscountNotRounded());
    }

    private BigDecimal getTip(BigDecimal totalAfterDiscount) {
        return getTip() == null ?
                BigDecimal.ZERO :
                getTip().getPercentage() != null ?
                        toPercentageRounded(totalAfterDiscount, getTip().getPercentage()) :
                        getTip().getAbsoluteValue();
    }

    @Override
    public BigDecimal getTotal() {
        return applyServiceCharge(getTotalBeforeServiceChange());
    }

    public BigDecimal getTotalBeforeServiceChange() {
        BigDecimal totalAfterDiscount = getTotalAfterDiscountNotRounded();
        BigDecimal tip = getTip(totalAfterDiscount);
        return totalAfterDiscount.add(tip).add(getVipCharge());
    }

    private BigDecimal getTotalAfterDiscountNotRounded() {
        BigDecimal itemsSum = sumPlainDrinkWithMixerPrice();
        return applyDiscountNotRounded(itemsSum);
    }

    @Override
    public BigDecimal sumPlainDrinkWithMixerPrice() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<OrderKey, OrderItemRequest> entry : getDrinkSelection().entrySet()) {
            OrderItemRequest item = entry.getValue();
            sum = sum.add(item.getRoundedTotalItemPrice());
        }
        return sum;
    }

    private BigDecimal applyDiscountNotRounded(BigDecimal originalPrice) {
        return getDiscount() == null ?
                originalPrice :
                MoneyUtils.applyDiscountNotRounded(originalPrice, getDiscount().getPercentage());
    }

    private BigDecimal applyServiceCharge(BigDecimal discountedPrice) {
        return MoneyUtils.applyServiceChargeRounded(discountedPrice, getServiceCharge());
    }

    @Override
    public BigDecimal getDiscountValue() {
        BigDecimal totalWithDiscountRounded = round(applyDiscountNotRounded(sumPlainDrinkWithMixerPrice()));
        return sumPlainDrinkWithMixerPrice().subtract(totalWithDiscountRounded);
    }

    @Override
    public void addDrink(Drink drink, boolean selected, List<DrinkOption> mixers, int count) {
        //TODO: make this compile time error
        throw new RuntimeException("Add drink not supported in preview mode");
    }

    @Override
    public <T extends NamedObject> void remove(Drink drink, boolean withIce, List<T> mixers) {
        //TODO: make this compile time error
        throw new RuntimeException("remove drink not supported in preview mode");
    }

    @Override
    public void notifyUpdate() {
        //TODO: make this compile time error
        throw new RuntimeException("notifyUpdate not supported in preview mode");
    }
}
