/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.NamedObject;

import java.util.List;

/**
 *
 */

public interface IDrinkItem<T extends NamedObject> {

    Drink getDrink();

    void increment(int increment);

    int getCount();

    boolean isWithIce();

    List<T> getSelectedMixers();

    void reset();
}
