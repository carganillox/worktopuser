/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.viewholder;

/**
 *
 */

public interface IItemUpdated<S> {

    void onUpdated();

    void onMerged();

    void itemAdded(S item);

    void itemRemoved(S item);
}
