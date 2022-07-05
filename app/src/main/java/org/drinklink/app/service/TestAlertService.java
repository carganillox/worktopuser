/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.service;

import android.content.Intent;

import androidx.annotation.NonNull;

/**
 *
 */
//adb -s emulator-5554 shell am startservice org.drinklink.app/.service.TestAlertService
//adb -s emulator-5556 shell am start-foreground-service org.drinklink.app/.service.TestAlertService
public class TestAlertService extends TestNotificationService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
    }

    @Override
    protected boolean isRingAlert() {
        return true;
    }
}