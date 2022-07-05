/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.viewholder.IItemUpdated;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.ui.viewmodel.SelectedDrinkPreviewItem;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.List;

import butterknife.BindView;
import lombok.Setter;

public class OrderSummaryHolder extends ViewModelBaseHolder<IOrderProcessor> implements IItemUpdated<OrderItemRequest> {

    private final View.OnClickListener onClickReset;
    private final View.OnClickListener onClickOrder;
    private final Activity activity;
    @Setter
    private Place place;

    @BindView(R.id.container_order_summary)
    View container;
    @BindView(R.id.order_amount)
    TextView amount;
    @BindView(R.id.btn_order)
    AppCompatButton btnOrder;
    @BindView(R.id.btn_reset)
    Button btnReset;
    @BindView(R.id.list_order_drinks)
    RecyclerView drinksView;
    @BindView(R.id.order_arrow)
    AppCompatImageView arrow;
    @BindView(R.id.reset_border)
    View resetBorder;
    @BindView(R.id.pay_button_container)
    View payButtonContainer;
    @BindView(R.id.order_summary_header)
    View orderSummaryHeader;

    ValueAnimator animator = new ValueAnimator() {
    };

    ViewModelAdapter<SelectedDrinkPreviewItem> adapter;
    SlidingUpPanelLayout.PanelSlideListener slideListener;
    SlidingUpPanelLayout slideLayout;
    AnimatedButtonHolder payButtonAnimationHolder;
    boolean collapsed = true;
    SlidingUpPanelLayout.PanelState panelState = SlidingUpPanelLayout.PanelState.COLLAPSED;

    public OrderSummaryHolder(View itemView, View.OnClickListener onClickOrder,
                              View.OnClickListener onClickReset, Activity activity, Place place) {
        super(itemView);
        this.onClickOrder = onClickOrder;
        this.onClickReset = onClickReset;
        this.activity = activity;
        this.place = place;
    }

    @Override
    public void bind(Context ctx, int position, IOrderProcessor orderItem) {
        super.bind(ctx, position, orderItem);
        this.payButtonAnimationHolder = new AnimatedButtonHolder(payButtonContainer);
        payButtonAnimationHolder.bind(ctx, position, false);
        setButtonText(ctx);
        bindSummary(ctx, item);

        adapter = getAdapter(getSelectedDrinkItems(), item);
        drinksView.setAdapter(adapter);
        setButtons();
    }

    public void setButtonText(Context ctx) {
        if (place != null) {
            btnOrder.setText(ctx.getString(R.string.btn_order, place.getTimeToCollect()));
        }
    }

    public void animate() {

        View animatedView = arrow;
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animatedView.getLayoutParams();
        animator = ValueAnimator.ofInt(-72, 72);
        animator.addUpdateListener(valueAnimator -> {
            params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
            animatedView.requestLayout();
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animatedView.getLayoutParams();
                params.bottomMargin = 0;
                animatedView.requestLayout();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.setDuration(1200);
        animator.setRepeatCount(15);
        if (panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            animator.start();
        }
    }

    private List<SelectedDrinkPreviewItem> getSelectedDrinkItems() {
        return ListUtil.transform(item.getOrderItems(), (i) -> transform(i));
    }

    public void register(SlidingUpPanelLayout slidingUpPanelLayout) {
        listen(slidingUpPanelLayout);
        item.registerListener(this);
    }

    private void setButtons() {
        boolean hasItems = adapter.getItemCount() > 0;
        setVisibility(btnReset, hasItems);
        setVisibility(resetBorder, hasItems);

        payButtonAnimationHolder.setButtons(hasItems);
    }

    public void unregister() {
        if (item != null) {
            item.unRegisterListener(this);
        }
        unregisterSlideListener(slideLayout);
    }

    private void bindSummary(Context ctx, IOrderProcessor item) {
        boolean hasItems = !item.isEmpty();
        btnOrder.setOnClickListener(onClickOrder);
        btnOrder.setEnabled(hasItems);
        btnReset.setOnClickListener(onClickReset);
        btnReset.setEnabled(hasItems);
        amount.setText(ctx.getString(R.string.order_amount_format_no_currency, item.sumPlainDrinkWithMixerPrice()));
//        int count = item.getCount();
//        String plural = count == 1 ? "" : "s";
//        items.setText(ctx.getString(R.string.order_items_format, count));
    }

    private ViewModelAdapter getAdapter(List<SelectedDrinkPreviewItem> orderItems, final IOrderProcessor processor) {

        DrinkListItemBaseHolder.CountTracker countTracker = new DrinkListItemBaseHolder.CountTracker();
        ViewModelAdapter adapter = new ViewModelAdapter(ctx, orderItems, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(SelectedDrinkPreviewItem.class, DrinkListItemSelectionHolder.getLayout(), view ->
                        new DrinkListItemSelectionHolder(view, processor, activity, countTracker));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    @Override
    public void onUpdated() {
        bindSummary(ctx, item);
    }

    @Override
    public void onMerged() {
        adapter.replaceItems(getSelectedDrinkItems());
        bindSummary(ctx, item);
        setButtons();
    }

    @Override
    public void itemAdded(OrderItemRequest item) {
        adapter.appendItem(transform(item));
    }

    @NonNull
    private SelectedDrinkPreviewItem transform(OrderItemRequest item) {
        return new SelectedDrinkPreviewItem(item);
    }

    @Override
    public void itemRemoved(OrderItemRequest item) {
        adapter.removeItem(transform(item));
        setButtons();
    }

    public static int getLayout() {
        return R.layout.include_order_summary;
    }

    private void listen(SlidingUpPanelLayout slidingUpPanelLayout) {
        unregisterSlideListener(slidingUpPanelLayout);
        slideLayout = slidingUpPanelLayout;

        slideLayout.setScrollableView(drinksView);
        slideLayout.setDragView(orderSummaryHeader);
//        slideLayout.setEnableDragViewTouchEvent(true);

        slideListener = new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelStateChanged(View view, SlidingUpPanelLayout.PanelState oldPanelState,
                                            SlidingUpPanelLayout.PanelState newPanelState) {
                if (collapsed && newPanelState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    arrow.setRotation(180);
                    collapsed = false;
                    animator.cancel();
                } else if (!collapsed && newPanelState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    arrow.setRotation(0);
                    collapsed = true;
                    animator.cancel();
                }
                OrderSummaryHolder.this.panelState = newPanelState;

            }
        };
        slidingUpPanelLayout.addPanelSlideListener(slideListener);
    }

    private void unregisterSlideListener(SlidingUpPanelLayout slidingUpPanelLayout) {
        if (slideListener != null && slidingUpPanelLayout != null) {
            slidingUpPanelLayout.removePanelSlideListener(slideListener);
        }
    }
}
