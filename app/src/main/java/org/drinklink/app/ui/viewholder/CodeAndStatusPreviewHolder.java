/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;

import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Order;


public class CodeAndStatusPreviewHolder extends CodeAndStatusHolder {

    public CodeAndStatusPreviewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void setHeaderDescription(Bar bar, Order order) {
        tvHeaderBarLocation.setText(order.getBartender());
        setVisibility(tvHeaderBarLocation, order.getBartender() != null);
    }

    @Override
    protected void setPreparedBy(Context ctx, Order order) {
    }
}
