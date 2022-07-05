/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.Drink;
import org.drinklink.app.model.DrinkCategory;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.List;

import lombok.Data;

/**
 *
 */
@Data
public class DrinkCategoryItem {

    private DrinkCategory category;

    private boolean isExpanded;

    private IOrderProcessor processor;

    public DrinkCategoryItem(DrinkCategory drinkCategory, IOrderProcessor processor) {
        this.category = drinkCategory;
        this.processor = processor;
    }

    public boolean hasSubCategories() {
        return !category.areSubcategoriesEmpty();
    }

    public boolean hasDrinks() {
        List<Drink> drinks = category.getDrinks();
        return drinks != null && !drinks.isEmpty();
    }
}
