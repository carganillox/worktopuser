/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.ui.viewmodel.DrinkOptionItem;

import butterknife.BindView;


public class DrinkOptionListItemHolder extends ViewModelBaseHolder<DrinkOptionItem> {

    @BindView(R.id.list_item)
    ToggleButton btnMixer;

    private Runnable optionChanged;

    public DrinkOptionListItemHolder(View itemView, Runnable optionChanged) {
        super(itemView);
        this.optionChanged = optionChanged;
    }

    @Override
    public void bind(Context ctx, final DrinkOptionItem item) {
        super.bind(ctx, item);
        this.ctx = ctx;

        btnMixer.setText(item.isSelected() ? item.getSelectedLabel() : item.getNotSelectedLabel());
        btnMixer.setTextOn(item.getSelectedLabel());
        btnMixer.setTextOff(item.getNotSelectedLabel());
        btnMixer.setOnCheckedChangeListener(getOnCheckedChangeListener());
        btnMixer.setEnabled(!isPreview());
    }

    protected boolean isPreview() {
        return false;
    }

    @NonNull
    protected CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return (compoundButton, checked) -> {
            item.setSelected(checked);
            optionChanged.run();
        };
    }

    public static int getLayout() {
        return R.layout.list_item_drink_option_ice;
    }
}
