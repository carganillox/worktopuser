/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.view.View;

import org.drinklink.app.R;


public class DrinkOptionCategoryPreviewListItemHolder extends DrinkOptionCategoryListItemHolder {

    public DrinkOptionCategoryPreviewListItemHolder(View itemView, Activity activity) {
        super(itemView, activity, null);
    }

    protected boolean isPreview() {
        return true;
    }

    public static int getLayout() {
        return R.layout.list_item_drink_option_category_preview;
    }
}
