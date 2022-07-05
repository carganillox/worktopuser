/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;
import org.drinklink.app.utils.DrinkIconUtils;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.List;

import butterknife.BindView;


public class CategoryListItemHolder extends ViewModelClickableHolder<DrinkCategoryItem> {

    private static final String COLLAPSED = "+";
    private static final String EXPANDED = "-";

    @BindView(R.id.list_item)
    View container;
    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.logo_drink_category)
    ImageView iconCategory;
    @BindView(R.id.list_sub_categories)
    RecyclerView list;
    @BindView(R.id.next_sub_categories)
    ImageView nextIndicator;
    @BindView(R.id.lbl_expand_collapse)
    TextView expandCollapse;

    public CategoryListItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, final DrinkCategoryItem item) {
        super.bind(ctx, item);
        this.ctx = ctx;

        setClickListenerWithHolder(container, onClick);
        int colorResId = item.isExpanded() ? R.color.darkcharcoal : R.color.charcoal;
        int color = ContextCompat.getColor(ctx, colorResId);
        container.setBackgroundColor(color);
        name.setText(item.getCategory().getName());

        setVisibility(nextIndicator, !item.hasSubCategories());
        setVisibility(expandCollapse, item.hasSubCategories());
        expandCollapse.setText(item.isExpanded() ? EXPANDED : COLLAPSED);

        int drawableResourceId = DrinkIconUtils.getDrinkIcon(ctx, item);
        iconCategory.setImageResource(drawableResourceId);

        list.setLayoutManager(new LinearLayoutManager(ctx));
        setVisibility(item.isExpanded(), list);

        List<DrinkCategoryItem> drinkItems = ListUtil.transform(item.getCategory().getSubCategories(),
                drinkCategory -> new DrinkCategoryItem(drinkCategory, item.getProcessor()));
        list.setAdapter(getAdapterInstance(ctx, drinkItems, item.getProcessor()));
    }

    protected ViewModelAdapter getAdapterInstance(Context ctx, List<DrinkCategoryItem> drinks, IOrderProcessor processor) {

        ViewModelAdapter adapter = new ViewModelAdapter(ctx, drinks, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(DrinkCategoryItem.class, SubCategoryListItemHolder.getLayout(),
                        view -> new SubCategoryListItemHolder(view, onClick));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    public static int getLayout() {
        return R.layout.list_item_category;
    }
}
