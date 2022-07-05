/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class Area extends NamedObject {

    public City city;
}
