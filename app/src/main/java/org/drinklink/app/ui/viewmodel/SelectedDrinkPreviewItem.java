/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.request.MixerRequest;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.List;

import lombok.Data;

/**
 *
 */
@Data
public class SelectedDrinkPreviewItem implements IDrinkItem<MixerRequest> {

    private OrderItemRequest orderItemRequest;

    public SelectedDrinkPreviewItem(OrderItemRequest orderItemRequest) {
        this.orderItemRequest = orderItemRequest;
    }

    @Override
    public Drink getDrink() {
        return orderItemRequest.getOriginalDrink();
    }

    @Override
    public int getCount() {
        return orderItemRequest.getQuantity();
    }

    @Override
    public void increment(int increment) {
        int quantity = Math.max(0, Math.min(getCount() + increment, IOrderProcessor.MAX_DRINK_COUNT));
        orderItemRequest.setQuantity(quantity < 0 ? 0 : quantity);
    }

    @Override
    public boolean isWithIce() {
        return orderItemRequest.isWithIce();
    }

    @Override
    public List<MixerRequest> getSelectedMixers() {
        return orderItemRequest.getSelectedMixers();
    }

    @Override
    public void reset() {

    }
}
