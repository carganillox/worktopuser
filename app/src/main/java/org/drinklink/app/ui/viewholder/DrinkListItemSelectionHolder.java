/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;

import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.DrinkOption;
import org.drinklink.app.model.DrinkOptionCategory;
import org.drinklink.app.model.request.MixerRequest;
import org.drinklink.app.ui.viewmodel.DrinkOptionItem;
import org.drinklink.app.ui.viewmodel.SelectedDrinkPreviewItem;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.util.ArrayList;
import java.util.List;


public class DrinkListItemSelectionHolder extends DrinkListItemBaseHolder<MixerRequest, SelectedDrinkPreviewItem, IOrderProcessorPreview> {

    public DrinkListItemSelectionHolder(View itemView, IOrderProcessorPreview processor, Activity activity, CountTracker countTracker) {
        super(itemView, processor, activity, countTracker, null);
    }

    @NonNull
    protected ViewModelAdapter getMixersAdapter(List items) {
        return new ViewModelAdapter(ctx, items, new ViewModelHolderFactory() {
            {
                add(DrinkOptionCategory.class, DrinkOptionCategoryPreviewListItemHolder.getLayout(),
                        view -> new DrinkOptionCategoryPreviewListItemHolder(view, activity));
                add(DrinkOptionItem.class, DrinkOptionPreviewListItemHolder.getLayout(),
                        view -> new DrinkOptionPreviewListItemHolder(view));
            }
        });
    }

    @Override
    protected boolean isPreview() {
        return true;
    }

    @Override
    protected void addMixerCategories(List mixerCategories, List trackedCategories, DrinkOptionCategory cat, List<DrinkOption> select) {
        for (DrinkOption oneSelection : select) {
            ArrayList<DrinkOption> oneSelectionList = new ArrayList<>();
            oneSelectionList.add(oneSelection);
            super.addMixerCategories(mixerCategories, trackedCategories, cat, oneSelectionList);
        }
    }

    @Override
    protected boolean increment(IOrderProcessorPreview processor, View view, int increment) {
        boolean isValid = super.increment(processor, view, increment);
        if (item.getCount() <= 0) {
            processor.remove(item.getDrink(), item.isWithIce(), item.getSelectedMixers());
        } else {
            processor.notifyUpdate();
        }
        return isValid;
    }

    protected int getTotalCount(IOrderProcessorPreview processor) {
        return processor.getCount();
    }

    @Override
    protected void setInfoVisibility(View description, boolean b) {
        super.setInfoVisibility(description, false);
    }
}
