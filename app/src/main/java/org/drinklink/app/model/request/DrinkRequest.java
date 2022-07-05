/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.NamedObject;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrinkRequest extends NamedObject {

    // This is maybe not necessary since it can be also a part of the name.
    private String volume;

    private Integer drinkCategoryId;

    private BigDecimal price;

    public DrinkRequest(Drink drink) {
        id = drink.getId();
        volume = drink.getVolume();
        drinkCategoryId = drink.getDrinkCategoryId();
        price = drink.getPrice();
    }
}
