/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.IItemUpdated;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.workflow.IOrderProcessor;

import butterknife.BindView;


public class OrderSummaryInfoHolder extends ViewModelBaseHolder<IOrderProcessor> implements IItemUpdated {

    @BindView(R.id.lbl_name)
    TextView name;
    @BindView(R.id.lbl_total_price)
    TextView sum;

    public OrderSummaryInfoHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, IOrderProcessor item) {
        super.bind(ctx, position, item);
        OrderRequest order = item.asOrderRequest(null, null);
        sum.setText("$ " + order.getFinalPrice());
    }

    public void register() {
        item.registerListener(this);
    }

    public void unregister() {
        if (item != null) {
            item.unRegisterListener(this);
        }
    }

    @Override
    public void onUpdated() {
        reBind();
    }

    @Override
    public void onMerged() {
        onUpdated();
    }

    @Override
    public void itemAdded(Object item) {

    }

    @Override
    public void itemRemoved(Object item) {

    }

    public static int getLayout() {
        return R.layout.include_order_summary_info;
    }
}
