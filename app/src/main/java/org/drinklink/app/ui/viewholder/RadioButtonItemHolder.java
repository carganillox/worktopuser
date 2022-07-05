/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.ui.viewmodel.RadioButtonItem;

import butterknife.BindView;


public class RadioButtonItemHolder<T extends RadioButtonItem> extends ViewModelClickableHolder<T> {

    @Nullable
    @BindView(R.id.radio_button)
    CompoundButton radioButton;

    @BindView(R.id.radio_addition)
    TextView tvAddition;

    public RadioButtonItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, T item) {
        super.bind(ctx, position, item);
        CompoundButton button = getButton();
        button.setText(item.getItem().getVisualName());
        button.setTag(item);

        // prevent on checked propagation when setting setChecked
        button.setOnCheckedChangeListener(null);
        button.setChecked(item.isChecked());
        button.setOnCheckedChangeListener(
                (compoundButton, checked) -> onClick.onClick(compoundButton));

        String addition = item.getItem().getVisualAddition();
        setVisibility(tvAddition, addition != null);
        tvAddition.setText(addition);
    }

    protected CompoundButton getButton() {
        return this.radioButton;
    }

    public static int getLayout() {
        return R.layout.list_item_radio_button;
    }
}
