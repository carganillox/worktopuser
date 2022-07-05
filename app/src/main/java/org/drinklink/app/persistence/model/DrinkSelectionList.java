/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence.model;

import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.workflow.OrderKey;

import java.util.ArrayList;
import java.util.Iterator;

import lombok.NoArgsConstructor;

/**
 *
 */

@NoArgsConstructor
@Deprecated
public class DrinkSelectionList extends ArrayList<DrinkSelection> {

    public DrinkSelectionList(ArrayList<DrinkSelection> list) {
        super(list);
    }

    public OrderItemRequest get(OrderKey orderKey) {
        DrinkSelection first = ListUtil.findFirst(this, item -> item.equals(orderKey));
        return first != null ? first.getOrderItemRequest() : null;
    }

    public OrderItemRequest removeByKey(OrderKey orderKey) {
        Iterator<DrinkSelection> iterator = iterator();
        OrderItemRequest removed = null;
        while(iterator.hasNext()) {
            DrinkSelection next = iterator.next();
            if (next.getOrderKey().equals(orderKey)) {
                removed = next.getOrderItemRequest();
                iterator.remove();
                break;
            }
        }
        return removed;
    }

    public void put(OrderKey newOrderKey, OrderItemRequest newItem) {
        removeByKey(newOrderKey);
        add(new DrinkSelection(newOrderKey, newItem));
    }
}
