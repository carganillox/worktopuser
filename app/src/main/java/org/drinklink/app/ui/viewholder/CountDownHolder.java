/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.utils.TimeUtils;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import lombok.Setter;


public class CountDownHolder extends ViewModelBaseHolder<IOrderProcessorPreview> {

    private static final String TAG = "CountdownHolder";

    public static long EXPIRED_BACKUP_TIME = TimeUnit.MINUTES.toMillis(1);
    public static final int DELAY_TIME = 1000;

    @Nullable
    @BindView(R.id.time_to_collect)
    TextView tvTimeToCollect;

    @Nullable
    @BindView(R.id.time_to_collect_message)
    TextView tvTimeToCollectMessage;

    @Nullable
    @BindView(R.id.time_to_collect_min)
    TextView timeToCollectMin;

    @Nullable
    @BindView(R.id.timer_description)
    TextView timerDescription;

    private CountDownTimer countDownTimer;
    int colorResId;
    private Runnable onStatusChange;
    @Setter
    private boolean isResumed;
    private OrderStates lastState;

    public CountDownHolder(View itemView, int colorResId, Runnable onStatusChange) {
        super(itemView);
        this.colorResId = colorResId;
        this.onStatusChange = onStatusChange;
        isResumed = true;
    }

    @Override
    public void bind(Context ctx, int position, IOrderProcessorPreview orderProcessor) {
        super.bind(ctx, position, orderProcessor);
        Order order = orderProcessor.getOrder();
        if (order == null) {
            Logger.i(TAG, "Order null!!!");
            return;
        }
        lastState = order.getCurrentOrderState();
        initTimeToCollectInternal(order);
    }

    public void initTimeToCollect(Order order) {
        lastState = order.getCurrentOrderState();
        initTimeToCollectInternal(order);
    }

    private void initTimeToCollectInternal(Order order) {
        // if this is called from delayed handler, check if activity is still active
        if (!isResumed) {
            Logger.w(TAG, "not resumed");
            return;
        }

        boolean isStarted = order.getCollectAtUtcMillis() > 0;
        long millisInFuture = isStarted ?
                order.getRemainingTimeMs() :
                TimeUnit.MINUTES.toMillis(item.getPlace().getTimeToCollect());

        if (millisInFuture > TimeUtils.MAX_MILLIS_TIME) {
            Logger.w(TAG, "retry initTimeToCollect");
            // delay because Time classes are not initialized jet, try in 1 second
            new Handler(Looper.myLooper()).postDelayed(() -> initTimeToCollectInternal(order), DELAY_TIME);
            return;
        }
        // already started counter or time not available jet
        setTime(millisInFuture, false, order);
        if (!isStarted) {
            return;
        }
        hideTimerDescription();
        if (countDownTimer != null) {
            if (order.isFinished() || order.isExpired()) {
                cancelTimer();
            }
            return;
        }
        long additional = Order.EXPIRED_TIME + EXPIRED_BACKUP_TIME;
        countDownTimer = new CountDownTimer(millisInFuture + additional, TimeUtils.SECOND_IN_MS) {
            @Override
            public void onFinish() {

            }

            @Override
            public void onTick(long l) {
                setTime(l - additional, l == 0, order);
            }
        };
        countDownTimer.start();
    }

    private void hideTimerDescription() {
        if (timerDescription != null) {
            setVisibility(timerDescription, false);
        }
    }

    public void setTime(long milliseconds,
                        boolean isTimeout,
                        Order order) {
        OrderStates currentOrderState = order.getCurrentOrderState();
        if (tvTimeToCollect != null) {
            boolean expired = isTimeout || order.isExpired();
            boolean collected = currentOrderState == OrderStates.Collected;
            String statusMessage = null;
            if (collected) {
                statusMessage = ctx.getString(OrderStates.Collected.getStringResId(order.isBarOrder()));
            } else if (expired) {
                statusMessage = "-" + TimeUtils.getTimeFormat(Math.abs(Order.EXPIRED_TIME));
                //ctx.getString(R.string.expired);
            }

            // show 00:00 when rejected
            boolean failed = order.isFailed();
            if (failed) {
                milliseconds = 0;
            }
            String time = TimeUtils.getTimeFormat(Math.abs(milliseconds));
            if (milliseconds < 0) {
                time = "-" + time;
            }

            // TODO: Ready or what is first state when milliseconds are set?
            if (!collected && (expired || milliseconds < 0 || failed)) {
                colorResId = R.color.ruddy;
            } else if (currentOrderState.getId() >= OrderStates.Ready.getId()) {
                colorResId = R.color.mango_tango;
            }

            int timerColor = ctx.getResources().getColor(colorResId);
            tvTimeToCollect.setTextColor(timerColor);
            // setText null instead of visibility to preserve layout
            tvTimeToCollect.setText(statusMessage == null ? time : null);
            timeToCollectMin.setTextColor(timerColor);
            tvTimeToCollectMessage.setTextColor(timerColor);
            boolean hasMessage = statusMessage != null;
            setVisibility(tvTimeToCollectMessage, hasMessage);
            tvTimeToCollectMessage.setText(statusMessage);
            setVisibility(tvTimeToCollect, !hasMessage);
            setVisibility(timeToCollectMin, !hasMessage);
            if (failed) {
                hideTimerDescription();
            }

            if (expired && countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
        if (lastState != currentOrderState) {
            Logger.i(TAG, "last State:" + lastState + ", new state" + currentOrderState);
            onStatusChange.run();
            lastState = currentOrderState;
        }
    }

    public void setRejectedOrCanceled() {
        cancelTimer();
        TextView textView = tvTimeToCollect;
        if (textView != null) {
            String time = TimeUtils.getTimeFormat(0);
            textView.setText(time);
            textView.setTextColor(ctx.getResources().getColor(R.color.ruddy));
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}
