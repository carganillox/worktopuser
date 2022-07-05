/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.view.View;
import android.widget.CompoundButton;

import org.drinklink.app.R;

import butterknife.BindView;


public class CheckBoxItemHolder extends RadioButtonItemHolder {

    @BindView(R.id.check_box_option)
    CompoundButton checkBoxButton;

    public CheckBoxItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    protected CompoundButton getButton() {
        return checkBoxButton;
    }

    public static int getLayout() {
        return R.layout.list_item_check_box;
    }
}
