/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.workflow;

import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.utils.ListUtil;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderKey {
    int drinkId;
    List<Integer> mixerIds;
    boolean withIce;

    public OrderKey(OrderItemRequest request) {
        this.drinkId = request.getDrink().getId();
        this.mixerIds = ListUtil.transform(request.getSelectedMixers(), item -> item.getId());
        this.withIce = request.isWithIce();
    }
}