/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import org.drinklink.app.R;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.utils.TextWatcherAdapter;

import butterknife.BindView;


public class CreditCardUsageHolder extends CreditCardPreviewHolder {

    @BindView(R.id.tv_ccv_code)
    TextView tvCcvCode;

    @BindView(R.id.et_ccv_code)
    EditText etCcvCode;

    @BindView(R.id.check_box_option)
    CheckBox checkBox;

    @BindView(R.id.card_logo)
    AppCompatImageView logo;


    public CreditCardUsageHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, CreditCardInfo item) {
        super.bind(ctx, position, item);
        itemView.setTag(item);
        DrinkLinkFragment.bindEditTextWithLabelVisibility(etCcvCode, tvCcvCode);
        etCcvCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                item.setCCV(charSequence.toString());
            }
        });
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(item.isChecked());
        checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            item.setChecked(checked);
            onClick.onClick(itemView);
        });

        logo.setImageResource(item.getLogo());
        itemView.setOnClickListener(view -> {
            checkBox.setChecked(!checkBox.isChecked());
        });
//        setVisibility(etCcvCode, item.isChecked());
//        setVisibility(tvCcvCode, item.isChecked());
    }

    public static int getLayout() {
        return R.layout.list_item_credit_card_usage;
    }
}
