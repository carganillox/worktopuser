/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import org.drinklink.app.R;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.persistence.DataStorage;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.activities.MainActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.utils.Analytics;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.TimeUtils;
import org.drinklink.app.workflow.IOrderProcessor;
import org.drinklink.app.workflow.OrderProcessor;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;



public class OrderPreparationItemPreview extends ViewModelBaseHolder<OrderPreparation> {

    private static final String TAG = "OrderPreparationItemPreview";
    public final static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
    private static SimpleDateFormat formatToString = new SimpleDateFormat("dd MMM yyyy");
    private final IOrderProcessor processor;
    private final View.OnClickListener onPreviewClick;
    private final Activity activity;
    private final String username;
    private final View.OnClickListener onDeleted;
    //DateFormat dateFormat = android.text.FORMAT.DateFormat.getDateFormat(ctx);

    @BindView(R.id.order_place)
    TextView orderPlace;
    @BindView(R.id.order_code)
    TextView orderCode;
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
    @BindView(R.id.order_date)
    TextView orderData;
    @Nullable
    @BindView(R.id.indicator_stripe)
    AppCompatButton indicatorStripe;
    @BindView(R.id.order_actions)
    View actionsView;
    @BindView(R.id.order_actions2)
    View actionsView2;
    @BindView(R.id.button_view_order)
    Button btnPreview;
    @BindView(R.id.button_delete)
    Button btnDelete;
    @BindView(R.id.order_created)
    TextView createdTime;


    public OrderPreparationItemPreview(View itemView, IOrderProcessor orderProcessor, Activity activity, String username, View.OnClickListener onPreviewClick, View.OnClickListener onDeleted) {
        super(itemView);
        this.processor = orderProcessor;
        this.onPreviewClick = onPreviewClick;
        this.onDeleted = onDeleted;
        this.activity = activity;
        this.username = username;
    }

    @Override
    public void bind(Context ctx, int position, OrderPreparation item) {
        super.bind(ctx, position, item);

        Order order = item.getOrder();

        orderPlace.setText(item.getPlace().getName());
        orderCode.setText(order.getCode());
        int count = order.getCount();
        orderItemsCount.setText(ctx.getString(count > 1 ? R.string.order_items_count_format : R.string.order_item_count_format, count));
        boolean showDeliveryStatus = !order.isFinished() && !order.isExpired() && order.isCollectTimeDefined();
        setVisibility(timeToCollectContainer, showDeliveryStatus && item.getOrder().isBarOrder());
        setVisibility(tableDelivery, showDeliveryStatus && !item.getOrder().isBarOrder());

        OrderStates currentOrderState = order.getCurrentOrderStateForPreview();
        orderStatus.setText(ctx.getString(currentOrderState.getStringResId(order.isBarOrder())));
        setColor(ctx, currentOrderState);

        long collectAtUtcMillis = order.getCollectAtUtcMillis();
        orderTimeToCollect.setText(TimeUtils.getTimeFormatForTime(collectAtUtcMillis));
        setOrderPickUp(item);
        Double totalPrice = order.getFinalPrice();
        orderAmount.setText(ctx.getString(R.string.order_amount_format, totalPrice));

        setDate(order);
        setActionButtonsVisibility();
        setClickListenerWithTag(btnPreview, item, onPreviewClick);
        btnDelete.setTag(item);
        createdTime.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(item.getCreated()));
    }

    protected void setActionButtonsVisibility() {
        setVisibility(actionsView, false);
        setVisibility(actionsView2, false);
    }

    private void setDate(Order order) {

        if (order.getTimestamp() == null) {
            return;
        }
        try {
            Date date = FORMAT.parse(order.getTimestamp());
            orderData.setText(formatToString.format(date));
        } catch (ParseException e) {
        }
    }

    protected void setColor(Context ctx, OrderStates currentOrderState) {
        int color = ContextCompat.getColor(ctx, currentOrderState.getColorResId());
        orderStatus.setTextColor(color);
    }

    private void setOrderPickUp(OrderPreparation item) {
        Bar bar = item.getBar();
        String description = bar == null ? null : (bar.getDescription() != null ? bar.getDescription() : bar.getName());
        setVisibility(orderPickUp, description != null);
        orderPickUp.setText(ctx.getString(R.string.order_pickup_format, description), TextView.BufferType.SPANNABLE);
        Spannable span = (Spannable) orderPickUp.getText();
        int colorWhite = ctx.getResources().getColor(R.color.white);
        span.setSpan(new ForegroundColorSpan(colorWhite), 0,
                ctx.getString(R.string.order_pickup_label).length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @OnClick(R.id.button_repeat_order)
    public void repeatOrder() {

        processor.reset();
        processor.forPlace(item.getPlace());
        processor.merge(new OrderProcessor(new OrderPreparation(item)));
        processor.save();

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(IntentUtils.CLEAR_AND_NEW);
        intent.putExtra(ExtrasKey.SHOW_STATUS, false);
        intent.putExtra(ExtrasKey.PLACE_ID_EXTRA, item.getPlace().getId());
        ctx.startActivity(intent);
    }

    @OnClick(R.id.button_refund)
    public void refundOrder() {
        new Analytics(ctx, TAG).refund();
        DialogManager.showInputDialog(activity,
                ctx.getString(R.string.dialog_title_refund),
                ctx.getString(R.string.dialog_message_refund),
                input -> sendEmail(input), () -> { });
    }

    @OnClick(R.id.button_delete)
    public void deleteOrder(View view) {
        if (item.getOrder().isFinished() ||
            item.getOrder().isExpired()) {
            DialogManager.showYesNoDialog(activity,
                    ctx.getString(R.string.delete_order_title),
                    ctx.getString(R.string.delete_order_message),
                    () -> {
                        processor.deleteOrderPreparation(item.getId());
                        onDeleted.onClick(view);
                    }, () -> {
                    });
        } else {
            DialogManager.showOkDialog(activity,
                    ctx.getString(R.string.delete_order_title),
                    ctx.getString(R.string.delete_order_pending));
        }
    }

    private void sendEmail(String input) {
        Order order = item.getOrder();

        String emailTo = ctx.getString(R.string.refund_email);
        String subject = "Refund request: " + order.getId();
        String finalPrice = ctx.getString(R.string.order_amount_format, order.getFinalPrice());
        String body = getBody(input, order, finalPrice);

        ShareCompat.IntentBuilder
                .from(activity)
                .setType("message/rfc822")
                .addEmailTo(emailTo)
                .setSubject(subject)
                .setText(body)
                //.setHtmlText(body) //If you are using HTML in your body text
                .setChooserTitle(ctx.getString(R.string.refund_email_chooser_text))
                .startChooser();

//        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailTo, null));
//        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
//        emailIntent.setType("message/rfc822");
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
//
//        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailTo, null));
//        emailIntent.setSelector( selectorIntent );
//
//        activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    @NotNull
    private String getBody(String input, Order order, String finalPrice) {
        return ctx.getString(R.string.refund_body, input,
                    Integer.toString(order.getId()),
                    notNull(order.getCode()),
                    order.getItemsPreview(),
                    finalPrice,
                    notNull(order.getTimeToCollect()),
                    username,
                    notNull(order.getOrderReference()));
    }

    private String notNull(String s) {
        return s != null ? s : "n/a";
    }

    public static int getLayout() {
        return R.layout.list_item_order_preparation;
    }
}
