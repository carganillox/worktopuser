/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.OrderStates;

import lombok.Data;
import lombok.Getter;

/**
 * Order cancellation
 */
@Data
public class OrderCancellation {

    public static final OrderCancellation INSTANCE = new OrderCancellation();

    @Getter
    private String newState = OrderStates.Canceled.name();
}
