/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import lombok.Data;

/**
 *
 */
@Data
public class OrderItemDrinkOptionRequest {

    private int id;

    private String name;

    private DrinkRequest mixer;

    private Integer mixerId;

    private double priceModifier;
}
