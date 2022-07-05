/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.NamedObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class CheckBoxItem<T extends NamedObject> extends RadioButtonItem<T> {

    public CheckBoxItem(int position, T item, boolean isChecked) {
        super(position, item, isChecked);
    }
}
