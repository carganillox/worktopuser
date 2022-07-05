/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 *
 */
public class DrinkSelectionItem implements IDrinkItem<DrinkOption> {

    private static final ArrayList<DrinkOption> EMPTY_LIST = new ArrayList<>();

    private Drink drink;
    @Getter
    private int count;

    public DrinkSelectionItem(Drink drink) {
        this.drink = drink;
    }

    @Override
    public Drink getDrink() {
        return drink;
    }

    @Override
    public void increment(int increment) {
        count = Math.max(0, Math.min(count + increment, IOrderProcessor.MAX_DRINK_COUNT));
    }

    @Override
    public boolean isWithIce() {
        return false;
    }

    @Override
    public List<DrinkOption> getSelectedMixers() {
        return EMPTY_LIST;
    }

    @Override
    public void reset() {
        count = 0;
    }
}
