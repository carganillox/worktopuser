/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;

import android.view.View;

import com.google.gson.Gson;

import org.drinklink.app.model.OrderStates;
import org.drinklink.app.persistence.DataStorage;
import org.drinklink.app.workflow.IOrderProcessor;


public class OrderPreparationItemHolder extends OrderPreparationItemPreview {

    public OrderPreparationItemHolder(View itemView, IOrderProcessor orderProcessor, Activity activity, String username, View.OnClickListener onPreviewClick, View.OnClickListener onDeleted) {
        super(itemView, orderProcessor, activity, username, onPreviewClick, onDeleted);
    }

    @Override
    protected void setActionButtonsVisibility() {
        setVisibility(actionsView, true);
    }

    @Override
    protected void setColor(Context ctx, OrderStates currentOrderState) {
        int color = ContextCompat.getColor(ctx, currentOrderState.getColorResId());
        orderStatus.setTextColor(color);
        ViewCompat.setBackgroundTintList(indicatorStripe, ColorStateList.valueOf(color));
    }
}
