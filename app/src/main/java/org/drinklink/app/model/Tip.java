/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class Tip extends NamedObject {

    public Tip(String name, int id, BigDecimal percentage) {
        this(name, id ,percentage, null, null);
    }

    public Tip(String name, int id, BigDecimal percentage, BigDecimal absoluteValue, String selectedName) {
        super(name, null);
        this.percentage = percentage;
        this.absoluteValue = absoluteValue;
        this.id = id;
        this.selectedName = selectedName;
    }

    /// <summary>
    /// Discount amount in percents.
    /// </summary>
    public BigDecimal percentage;

    public BigDecimal absoluteValue;

    public String selectedName;

    public String getSelectedName() {
        return selectedName != null ? selectedName : super.getSelectedName();
    }
}