/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Discount;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.Place;
import org.drinklink.app.utils.StringUtils;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import butterknife.BindView;

import static org.drinklink.app.utils.MoneyUtils.greaterThanZero;


public class CodeAndStatusHolder extends ViewModelBaseHolder<IOrderProcessorPreview> {

    @BindView(R.id.header_code_letter)
    TextView tvHeaderCodeLetter;

    @BindView(R.id.header_code_number)
    TextView tvHeaderCodeNumber;

    @BindView(R.id.header_code_and_status_description)
    TextView tvPlaceDsc;

    @BindView(R.id.header_barman)
    TextView tvHeaderBarLocation;

    @Nullable
    @BindView(R.id.prepared_by)
    TextView tvPreparedBy;

    @Nullable
    @BindView(R.id.discount_message)
    View discountMessage;

    @Nullable
    @BindView(R.id.discount_info)
    TextView discountInfo;

    @Nullable
    @BindView(R.id.discount_border)
    View discountAndTimerBorder;

    @Nullable
    @BindView(R.id.time_to_collect_container)
    View timeToCollectContainer;

    @Nullable
    @BindView(R.id.time_to_collect_margin)
    View timeToCollectMargin;

    public CodeAndStatusHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, IOrderProcessorPreview orderProcessor) {
        super.bind(ctx, position, orderProcessor);

        Place place = orderProcessor.getPlace();
        Order order = orderProcessor.getOrder();
        Bar bar = orderProcessor.getBar();
        tvPlaceDsc.setText(place.getName());
        tvHeaderCodeLetter.setText(StringUtils.getSubstring(order.getCode(), 0, 1));
        tvHeaderCodeNumber.setText(StringUtils.getSubstring(order.getCode(), 1, 3));
        setPreparedBy(ctx, order);
        setHeaderDescription(bar, order);

        boolean hasDiscount = greaterThanZero(orderProcessor.getDiscountValue());
        setVisibility(discountMessage, hasDiscount);
        setVisibility(discountInfo, hasDiscount);
        Discount discount = orderProcessor.getDiscount();
        setText(discountInfo, discount != null ? discount.getName() : null);
        boolean isTable = !order.isBarOrder();
        setVisibility(timeToCollectContainer, !isTable);
        setVisibility(timeToCollectMargin, hasDiscount && isTable);
        setVisibility(discountAndTimerBorder, hasDiscount || !isTable);
    }

    protected void setHeaderDescription(Bar bar, Order order) {
        tvHeaderBarLocation.setText(bar != null ? bar.getDescription() : null);
        setVisibility(tvHeaderBarLocation, bar != null);
    }

    protected void setPreparedBy(Context ctx, Order order) {
        tvPreparedBy.setText(ctx.getString(R.string.status_prepared_by, order.getBartender()));
        setVisibility(tvPreparedBy, order.getBartender() != null);
    }

    public static int getLayout() {
        return R.layout.include_code_and_status;
    }
}
