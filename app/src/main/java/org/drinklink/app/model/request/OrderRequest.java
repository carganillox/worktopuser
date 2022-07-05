/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.BillingAddress;
import org.drinklink.app.model.CreditCardInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Order request
 */
@Data
public class OrderRequest {

    private boolean saveCardInfo;

    public OrderRequest() {
        items = new ArrayList<>();
    }

    private List<OrderItemRequest> items;

    private Integer discountId;

    private BigDecimal tip = BigDecimal.ZERO;

    private BigDecimal originalPrice = BigDecimal.ZERO;

    private BigDecimal finalPrice = BigDecimal.ZERO;

    private Integer barId;

    private Integer tableId;

    private int facilityId;

    private boolean isVip;

    private BigDecimal vipCharge = BigDecimal.ZERO;

    private CreditCardInfo cardInfo;

    private BillingAddress billingAddress;

    private BigDecimal serviceCharge = BigDecimal.ZERO;
}
