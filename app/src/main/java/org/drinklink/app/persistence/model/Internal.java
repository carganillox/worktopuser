/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For field used internally to client app
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Internal {
}
