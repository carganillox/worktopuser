/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.utils.Logger;

import lombok.NoArgsConstructor;

/**
 *
 */
@NoArgsConstructor
public class NotificationsTokenUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationsTokenUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "on user present, trigger token upload");
        DependencyResolver.getComponent(context).getNotificationsTokenUpdateService().subscribe();
    }
}
