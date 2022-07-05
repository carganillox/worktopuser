/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence.model;

import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.workflow.OrderKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrinkSelection {
    private OrderKey orderKey;
    private OrderItemRequest orderItemRequest;
}
