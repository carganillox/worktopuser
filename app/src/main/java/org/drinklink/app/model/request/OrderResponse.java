/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model.request;

import org.drinklink.app.model.Order;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Order response. Order request is used when creating order. Order is internal to client app.
 * Order response is used when fetching order from BE
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderResponse extends Order {
}
