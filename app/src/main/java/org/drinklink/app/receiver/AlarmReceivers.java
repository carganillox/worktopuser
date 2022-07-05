/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.utils.Logger;

/**
 *
 */

public class AlarmReceivers extends BroadcastReceiver {

    private static final String TAG = "AlarmReceivers";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        int orderId = intent.getExtras().getInt(ExtrasKey.ORDER_ID_EXTRA);
        Logger.i(TAG, "Alarm for order: " + orderId);
//        DependencyResolver.getComponent().getOrderNotificationsManager().alertOrderReady(orderId);
    }
}
