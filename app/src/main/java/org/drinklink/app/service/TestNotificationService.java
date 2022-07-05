/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.service;

import android.app.Notification;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import org.drinklink.app.model.Order;
import org.drinklink.app.utils.Logger;

import java.util.Random;

/**
 *
 */
//adb -s emulator-5554 shell am startservice org.drinklink.app/.service.TestNotificationService
//adb -s emulator-5556 shell am start-foreground-service org.drinklink.app/.service.TestNotificationService
public class TestNotificationService extends JobIntentService {

    private static final String TAG = "TestNotificationService";

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isRingAlert = isRingAlert();
        boolean soundAlert = true;
        boolean soundNotification = true;

        String channelId = DrinkLinkFirebaseMessagingNotificationsService.getChannel(getApplicationContext(), isRingAlert, soundNotification, soundAlert);

        Logger.i(TAG, String.format("Test notification, channelId: %s, isRinging: %b, isAlarmSound %b, isNotificationSound: %b",
                channelId, isRingAlert, soundAlert, soundNotification));

        Order order = new Order();
        order.setId(new Random().nextInt());
        Notification notification = DrinkLinkFirebaseMessagingNotificationsService.getNotification(getApplicationContext(),
                order, isRingAlert, soundNotification, soundAlert, channelId);

        startForeground(1, notification);
    }

    protected boolean isRingAlert() {
        return false;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
    }
}