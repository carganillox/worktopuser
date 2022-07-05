/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.persistence.model.Internal;
import org.drinklink.app.utils.TimeUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemRequest extends NamedObject {

    @Internal
    private Drink originalDrink;

    @Internal
    private long timestamp;

    public OrderItemRequest() {
        timestamp = TimeUtils.getCurrentTimeMs();
    }

    public static final OrderItemRequest PLACEHOLDER = new OrderItemRequest();

    private DrinkRequest drink;

    private List<MixerRequest> selectedMixers;

    private int quantity;

    private BigDecimal price = BigDecimal.ZERO;

    private boolean withIce;

    public BigDecimal getRoundedTotalItemPrice() {
        return getOrderItemPrice().multiply(new BigDecimal(quantity));
    }

    public BigDecimal getOrderItemPrice() {
        BigDecimal mixersPrice = BigDecimal.ZERO;
        for (MixerRequest req: getSelectedMixers()) {
            mixersPrice =  mixersPrice.add(req.getPrice());
        }
        return getDrink().getPrice().add(mixersPrice);
    }

    public List<MixerRequest> getSelectedMixers() {
        if (selectedMixers == null) {
            selectedMixers = new ArrayList<>();
        }
        return selectedMixers;
    }

    @Override
    public String getVisualName() {
        NamedObject drink = this.originalDrink != null ? this.originalDrink : this.drink;
        String visualName = drink.getVisualName();
        if (!getSelectedMixers().isEmpty()) {
            visualName +=  " (" + getPreview(getSelectedMixers())+ ")";
        }
        return visualName;
    }

    @Override
    public String getSeparator() {
        return System.getProperty("line.separator");
    }

    public int getDrinkId() {
        return drink != null ? drink.getId() : originalDrink != null ? originalDrink.getId() : -1;
    }
}
