/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import lombok.Data;

/**
 *
 */
@Data
public class DrinkOptionItem {

    private boolean isSelected;

    private String selectedLabel;

    private String notSelectedLabel;
}
