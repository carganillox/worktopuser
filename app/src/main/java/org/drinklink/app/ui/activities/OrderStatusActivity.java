/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.ui.fragments.OrderStatusFragment;
import org.drinklink.app.utils.IntentUtils;
import org.jetbrains.annotations.NotNull;

/**
 *
 */

public class OrderStatusActivity extends ToolbarActivity {

    @NotNull
    public static Intent getOrderPreviewActivity(Context context, int orderId, boolean alert) {
        Intent intent = new Intent(context, OrderStatusActivity.class);
        intent.setFlags(IntentUtils.CLEAR_AND_NEW);
        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, orderId);
        intent.putExtra(ExtrasKey.SHOW_ORDER_READY_ALERT_EXTRA, alert);
        intent.putExtra(ExtrasKey.CAN_GO_BACK_EXTRA, false);
        return intent;
    }

    @NotNull
    public static Intent getOrderPreviewFromNotificationActivity(Context context, int orderId, boolean alert) {
        Intent intent = new Intent(context, OrderStatusActivity.class);
        intent.setFlags(IntentUtils.NEW_TASK);
        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, orderId);
        intent.putExtra(ExtrasKey.SHOW_ORDER_READY_ALERT_EXTRA, alert);
        intent.putExtra(ExtrasKey.CAN_GO_BACK_EXTRA, true);
        return intent;
    }

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return OrderStatusFragment.class;
    }

    @Override
    protected boolean displayBackArrow() {
        return canGoBack();
    }

    @Override
    protected boolean addToBackStack() {
        return !canGoBack();
    }

    private boolean canGoBack() {
        return getIntent().getExtras().getBoolean(ExtrasKey.CAN_GO_BACK_EXTRA, false);
    }
}
