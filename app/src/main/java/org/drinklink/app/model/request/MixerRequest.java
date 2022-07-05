/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.IPrice;
import org.drinklink.app.model.NamedObject;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static org.drinklink.app.utils.MoneyUtils.round;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MixerRequest extends NamedObject implements IPrice {

    private BigDecimal price = BigDecimal.ZERO;

    public MixerRequest(DrinkOption mixer) {
        setId(mixer.getId());
        setPrice(mixer.getPrice());
        setName(mixer.getVisualName());
    }

    @Override
    public String getVisualAddition() {
        return "(" + round(getPrice()) + " AED)";
    }
}
