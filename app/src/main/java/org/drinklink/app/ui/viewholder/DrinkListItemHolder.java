/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.view.View;

import org.drinklink.app.common.viewholder.UpdateSingleFunction;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.ui.viewmodel.DrinkSelectionItem;
import org.drinklink.app.workflow.IOrderProcessor;


public class DrinkListItemHolder extends DrinkListItemBaseHolder<DrinkOption, DrinkSelectionItem, IOrderProcessor> {

    public DrinkListItemHolder(View itemView, IOrderProcessor processor, Activity activity, CountTracker countTracker, UpdateSingleFunction<Integer, Boolean> onUpdate) {
        super(itemView, processor, activity, countTracker, onUpdate);
    }
}
