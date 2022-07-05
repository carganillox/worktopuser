/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.ui.activities.OrderStatusActivity;
import org.drinklink.app.utils.StringUtils;
import org.drinklink.app.utils.TimeUtils;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.text.DateFormat;

import butterknife.BindView;


public class NavigationOrderHolder extends ViewModelBaseHolder<IOrderProcessorPreview> {

    private final Runnable closeDrawer;
    @BindView(R.id.order_place)
    TextView orderPlace;
    @BindView(R.id.order_code_letter)
    TextView orderCodeLetter;
    @BindView(R.id.order_code_number)
    TextView orderCodeNumber;
    @BindView(R.id.order_items_count)
    TextView orderItemsCount;
    @BindView(R.id.order_time_to_collect)
    TextView orderTimeToCollect;
    @BindView(R.id.order_pick_up)
    TextView orderPickUp;
    @BindView(R.id.order_amount)
    TextView orderAmount;
    @BindView(R.id.order_time_to_collect_container)
    LinearLayout timeToCollectContainer;
    @BindView(R.id.table_delivery)
    View tableDelivery;
    @BindView(R.id.order_status)
    TextView orderStatus;
    @BindView(R.id.order_created)
    TextView createdTime;

    public NavigationOrderHolder(View itemView, Runnable closeDrawer) {
        super(itemView);
        this.closeDrawer = closeDrawer;
    }

    @Override
    public void bind(Context ctx, final IOrderProcessorPreview item) {
        super.bind(ctx, item);
        IOrderProcessorPreview orderProcessor = item;
        this.ctx = ctx;

        Order order = orderProcessor.getOrder();

        setClickListenerWithTag(itemView, (view) -> {
            showDetails(order.getId());
        });

        orderPlace.setText(orderProcessor.getPlace().getName());
        orderCodeLetter.setText(StringUtils.getSubstring(order.getCode(), 0, 1));
        orderCodeNumber.setText(StringUtils.getSubstring(order.getCode(), 1, 3));
        int count = order.getCount();
        orderItemsCount.setText(ctx.getString(count > 1 ? R.string.order_items_count_format : R.string.order_item_count_format, count));
        boolean showDeliveryStatus = !order.isExpired() && order.isCollectTimeDefined();
        setVisibility(timeToCollectContainer, showDeliveryStatus && orderProcessor.getOrder().isBarOrder());
        setVisibility(tableDelivery, showDeliveryStatus && !orderProcessor.getOrder().isBarOrder());

        long collectAtUtcMillis = order.getCollectAtUtcMillis();
        orderTimeToCollect.setText(TimeUtils.getTimeFormatForTime(collectAtUtcMillis));
        setPickup(ctx, orderProcessor, order);
        orderAmount.setText(ctx.getString(R.string.order_amount_format, orderProcessor.getTotal()));
        OrderStates currentOrderState = order.getCurrentOrderStateForPreview();
        orderStatus.setText(ctx.getString(currentOrderState.getStringResId(order.isBarOrder())));
        orderStatus.setTextColor(ContextCompat.getColor(ctx, currentOrderState.getColorResId()));
        createdTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(item.getCreated()));
    }

    private void setPickup(Context ctx, IOrderProcessorPreview orderProcessor, Order order) {
        Bar bar = orderProcessor.getBar();
        String description = bar == null ? null : (bar.getDescription() != null ? bar.getDescription() : bar.getName());
        boolean showPickup = description != null && !order.isFinished();
        setVisibility(orderPickUp, showPickup);
        orderPickUp.setText(ctx.getString(R.string.order_pickup_format, description), TextView.BufferType.SPANNABLE);
        Spannable span = (Spannable) orderPickUp.getText();
        int colorWhite = ctx.getResources().getColor(R.color.white);
        span.setSpan(new ForegroundColorSpan(colorWhite), 0,
                ctx.getString(R.string.order_pickup_label).length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void showDetails(int orderId) {
//        Intent intent = new Intent(ctx, CodeAndStatusActivity.class);
        Intent intent = OrderStatusActivity.getOrderPreviewActivity(ctx, orderId, false);
        closeDrawer.run();
        ctx.startActivity(intent);
    }

    public void setViewVisibility(boolean isVisible) {
        setVisibility(itemView, isVisible);
    }

    public static int getLayout() {
        return R.layout.navigation_order_item;
    }
}
