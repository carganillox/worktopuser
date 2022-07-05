/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;
import org.drinklink.app.utils.DrinkIconUtils;

import butterknife.BindView;

public class SubCategoryListItemHolder extends ViewModelClickableHolder<DrinkCategoryItem> {

    @BindView(R.id.list_item)
    View container;
    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.next_sub_categories)
    ImageView nextIndicator;
    @BindView(R.id.logo_drink_category)
    ImageView iconCategory;

    public SubCategoryListItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, final DrinkCategoryItem item) {
        super.bind(ctx, item);
        this.ctx = ctx;

        setClickListenerWithHolder(container, onClick);
        name.setText(item.getCategory().getName());

        int drawableResourceId = DrinkIconUtils.getDrinkIcon(ctx, item);
        iconCategory.setImageResource(drawableResourceId);
    }

    public static int getLayout() {
        return R.layout.list_item_sub_category;
    }
}
