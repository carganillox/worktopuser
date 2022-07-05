/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FirstNavigationDrinkCategory extends NavigationDrinkCategory {

    public FirstNavigationDrinkCategory(NavigationDrinkCategory navigationDrinkCategory) {
        this.name = navigationDrinkCategory.name;
        this.isLast = navigationDrinkCategory.isLast;
        this.setTag(navigationDrinkCategory.getTag());
    }
}
