/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.DrinkCategory;
import org.drinklink.app.workflow.IOrderProcessor;

/**
 *
 */
public class DrinkCategorySubItem extends DrinkCategoryItem {

    public DrinkCategorySubItem(DrinkCategory drinkCategory, IOrderProcessor processor) {
        super(drinkCategory, processor);
    }
}
