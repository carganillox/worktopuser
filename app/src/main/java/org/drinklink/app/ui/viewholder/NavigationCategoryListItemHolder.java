/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.ui.viewmodel.NavigationDrinkCategory;

import butterknife.BindView;


public class NavigationCategoryListItemHolder<T extends NavigationDrinkCategory> extends ViewModelClickableHolder<T> {

    @BindView(R.id.list_item)
    View container;
    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.next_sub_categories)
    View hasNext;


    public NavigationCategoryListItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, final T item) {
        super.bind(ctx, item);
        this.ctx = ctx;

        name.setText(item.getName());

        setClickListenerWithHolder(container, onClick);
        setVisibility(hasNext, !item.isLast());
    }

    public static int getLayout() {
        return R.layout.list_item_navigation_category;
    }

    public static int getLayoutFirst() {
        return R.layout.list_item_navigation_category_first;
    }
}
