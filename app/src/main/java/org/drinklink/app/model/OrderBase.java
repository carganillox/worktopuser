/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import org.drinklink.app.model.request.OrderItemRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Order request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderBase extends NamedObject {

    private boolean saveCardInfo;

    private List<OrderItemRequest> items;

    private Integer discountId;

    private double tip;

    private double originalPrice;

    private double finalPrice;

    private Integer barId;

    private Integer tableId;

    private int facilityId;

    private boolean isVip;

    private double vipCharge;

    public List<OrderItemRequest> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
}
