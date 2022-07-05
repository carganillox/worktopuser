/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.model.CreditCardInfo;

import butterknife.BindView;


public class CreditCardItemHolder extends ViewModelClickableHolder<CreditCardInfo> {

    @BindView(R.id.btn_saved_card)
    Button name;
    @BindView(R.id.et_name_on_card)
    TextView nameOnCard;
    @BindView(R.id.et_card_number)
    TextView cardNumber;
    @BindView(R.id.et_expiry)
    TextView expiry;
    @BindView(R.id.et_ccv)
    TextView ccv;
    @BindView(R.id.cb_remember)
    CheckBox remember;

    public CreditCardItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, CreditCardInfo item) {
        //
//        super.bind(ctx, position, item);
//        name.setText(item.getName());
//        boolean saved = item.isSaved();
//        setVisibility(name, saved);
//        setVisibility(!saved, nameOnCard, cardNumber, expiry, ccv, remember);
//
//        setClickListenerWithTag(name, item, onClick);
//        remember.setOnCheckedChangeListener((compoundButton, isChecked) -> {
//            item.setRemember(isChecked);
//        });
    }

    public static int getLayout() {
        return R.layout.list_item_credit_card;
    }
}
