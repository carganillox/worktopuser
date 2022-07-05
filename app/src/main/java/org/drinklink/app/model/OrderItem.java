package org.drinklink.app.model;

import org.drinklink.app.model.request.OrderItemRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderItem extends NamedObject {

    public Drink drink;

    public List<DrinkOption> options;

    public int quantity;

    public BigDecimal price = BigDecimal.ZERO;

    public OrderItem(OrderItemRequest item) {
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.id = 1;
    }

    public BigDecimal getOrderItemPrice() {
        return new BigDecimal(quantity).multiply(price);
    }

    public List<DrinkOption> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    @Override
    public String getVisualName() {
        String visualName = drink.getVisualName();
        if (!getOptions().isEmpty()) {
            visualName +=  " (" + getPreview(getOptions())+ ")";
        }
        return visualName;
    }

    @Override
    public String getSeparator() {
        return System.getProperty("line.separator");
    }
}