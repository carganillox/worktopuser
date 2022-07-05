package org.drinklink.app.model;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static org.drinklink.app.utils.MoneyUtils.round;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrinkOption extends NamedObject implements IPrice {

    public BigDecimal price = BigDecimal.ZERO;

    @Override
    public String getVisualAddition() {
        return "(" + round(getPrice()) + " AED)";
    }
}
