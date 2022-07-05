/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.viewholder;

/**
 *
 */

public interface UpdateSingleFunction<T, R> {

    UpdateSingleFunction PLACEHOLDER = o -> null;

    R apply(T t);
}
