/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.model.CreditCardInfo;

import butterknife.BindView;


public class CreditCardPreviewHolder extends ViewModelClickableHolder<CreditCardInfo> {

    @BindView(R.id.et_name_on_card)
    TextView nameOnCard;
    @BindView(R.id.et_card_number)
    TextView cardNumber;
    @BindView(R.id.card_logo)
    AppCompatImageView cardLogo;
    @Nullable
    @BindView(R.id.btn_delete_card)
    Button deleteButton;

    public CreditCardPreviewHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, CreditCardInfo item) {
        super.bind(ctx, position, item);
        if (deleteButton != null) {
            setClickListenerWithTag(deleteButton, item, onClick);
        }
        nameOnCard.setText(item.getCardholderName());
//        "2023-08"
        String year = item.getExpiry().substring(2,4);
        String month = item.getExpiry().substring(5,7);
        cardNumber.setText(item.getMaskedPan().substring(item.getMaskedPan().length() - 8) + " " + year + "/" + month);
        cardLogo.setImageResource(getCardLogo(item));

    }

    private int getCardLogo(CreditCardInfo creditCardItem) {
        return creditCardItem.getLogo();
    }

    public static int getLayout() {
        return R.layout.list_item_credit_card_preview;
    }
}
