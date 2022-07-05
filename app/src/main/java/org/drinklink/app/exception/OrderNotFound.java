package org.drinklink.app.exception;

import lombok.Getter;

/**
 *
 */
@Getter
public class OrderNotFound extends RuntimeException {

    private int orderId;

    public OrderNotFound(int orderId) {
        this.orderId = orderId;
    }
}
