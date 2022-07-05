/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.view.View;

import org.drinklink.app.R;


public class DrinkOptionPreviewListItemHolder extends DrinkOptionListItemHolder {

    public DrinkOptionPreviewListItemHolder(View itemView) {
        super(itemView, () -> {});
    }

    @Override
    protected boolean isPreview() {
        return true;
    }

    public static int getLayout() {
        return R.layout.list_item_drink_option_ice_preview;
    }
}
