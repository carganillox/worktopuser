package org.drinklink.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderState extends IdObject {

    public int state;

    public Order order;

    public String timestamp;

    public OrderStates getStateValue() {
        return OrderStates.getOrderStateById(state);
    }
}
