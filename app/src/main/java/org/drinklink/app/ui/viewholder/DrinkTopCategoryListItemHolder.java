/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;

import butterknife.BindView;


public class DrinkTopCategoryListItemHolder extends ViewModelClickableHolder<DrinkCategoryItem> {

    private static final RequestOptions GLIDE_OPTIONS = new RequestOptions().centerCrop()
            .placeholder(R.drawable.place_placeholder);

    @BindView(R.id.list_item)
    View container;
    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.logo_drink_category)
    ImageView logoPlace;
    @BindView(R.id.btn_add_drinks)
    Button btnAddDrinks;

    public DrinkTopCategoryListItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, final DrinkCategoryItem item) {
        super.bind(ctx, item);
        this.ctx = ctx;

        name.setText(item.getCategory().getName());

//        String url = item.getCategory().getImage().getUrl();
//        load(url).apply(GLIDE_OPTIONS).into(logoPlace);
        logoPlace.setImageResource(R.drawable.ico_drinks_moktails);

        setClickListenerWithHolder(btnAddDrinks, onClick);
    }

    public static int getLayout() {
        return R.layout.list_item_drink_top_category;
    }
}
