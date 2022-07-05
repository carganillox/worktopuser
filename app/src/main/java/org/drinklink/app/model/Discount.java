package org.drinklink.app.model;

import org.drinklink.app.utils.MoneyUtils;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Discount extends NamedObject {

//    public DiscountTypes discountType;

    /// <summary>
    /// Discount amount in percents.
    /// </summary>
    public BigDecimal percentage = BigDecimal.ZERO;

    @Override
    public String getVisualName() {
        return getName() + ", " + getPercentageString() + "%";
    }

    private String getPercentageString() {
        return MoneyUtils.isWholeNumber(percentage) ?
               String.format("%d",percentage.intValue()) :
               String.format("%s",percentage.doubleValue());
    }
}