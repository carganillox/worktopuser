/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.ui.viewmodel.LabelItem;

import butterknife.BindView;


public class LabeltemHolder extends ViewModelBaseHolder<LabelItem> {

    @BindView(R.id.lbl_name)
    TextView name;

    public LabeltemHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, LabelItem item) {
        super.bind(ctx, position, item);
        name.setText(item.getName());
    }

    public static int getLayoutSelectCard() {
        return R.layout.list_item_label_select_card;
    }
}
