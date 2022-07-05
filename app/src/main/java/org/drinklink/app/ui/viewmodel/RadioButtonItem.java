/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.NamedObject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 */
@Data
@AllArgsConstructor
public class RadioButtonItem<T extends NamedObject> {

    private int position;

    private T item;

    private boolean isChecked;
}
