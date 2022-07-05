/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelClickableHolder;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.ui.viewmodel.StateItem;
import org.drinklink.app.utils.Logger;

import butterknife.BindView;


public class StateItemHolder<T extends StateItem> extends ViewModelClickableHolder<StateItem> {

    private static final String TAG = "StateItemHolder";

    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.btn_cancel_state)
    Button btnCancel;
    @BindView(R.id.state_indicator)
    ImageView stateIndicator;
    @BindView(R.id.state_border)
    View border;
    @BindView(R.id.lbl_rejected_msg)
    TextView txtRejectedMsg;
    @BindView(R.id.collection_point_description)
    TextView txtCollectionPoint;
    @BindView(R.id.need_to_present_discount)
    TextView txtNeedDiscount;


    public StateItemHolder(View itemView, View.OnClickListener onClick) {
        super(itemView, onClick);
    }

    @Override
    public void bind(Context ctx, int position, StateItem stateItem) {
        super.bind(ctx, position, stateItem);

        StateItem item = stateItem.get();

        Logger.i(TAG, "State Item Holder " + item.getStateName() +
                " cancelable: " + item.isCancelable() +
                " isRejected: " + item.isRejected() +
                " executed: " + item.isExecuted() +
                " lastExecuted: " + item.isLastExecuted());

        name.setText(item.getName());

        btnCancel.setOnClickListener(onClick);
        setVisibility(btnCancel, item.isCancelable() && item.isLastExecuted());

        setVisibility(border, item.isLastExecuted());

        stateIndicator.setImageResource(
                item.isRejected() ? R.drawable.status_yes_1 :
                                    item.isExecuted() ? R.drawable.status_yes : R.drawable.status_pending);

        boolean isRejected = item.isStateMatch(OrderStates.Rejected);
        setVisibility(txtRejectedMsg, isRejected);
        if (isRejected) {
            txtRejectedMsg.setText(item.getBarmen() != null ?
                    ctx.getString(R.string.state_item_order_rejected_message, item.getBarmen(), item.getMessage()) :
                    item.getMessage());
        }

        boolean isReady = item.isStateMatch(OrderStates.Ready);
        txtCollectionPoint.setText(item.getBarDescription());
        setVisibility(txtCollectionPoint, isReady && item.getBarDescription() != null);
        setVisibility(txtNeedDiscount, item.isHasDiscount());
        txtNeedDiscount.setText(ctx.getString(item.isTable() ? R.string.need_discount_table : R.string.need_to_present_discount));
    }

    public static int getLayout() {
        return R.layout.list_item_state;
    }
}
