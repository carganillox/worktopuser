/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.viewholder;

/**
 *
 */

public interface UpdateFunction<S, T, R> {

    R apply(S s, T t);
}
