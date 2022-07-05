/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;

import org.drinklink.app.dependency.ApplicationComponent;
import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.dependency.DaggerApplicationComponent;
import org.drinklink.app.dependency.DaggerUpdateComponent;
import org.drinklink.app.dependency.UpdateComponent;
import org.drinklink.app.dependency.UpdateModule;
import org.drinklink.app.patch.PatchManager;
import org.drinklink.app.utils.Logger;
import org.jetbrains.annotations.NotNull;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

/**
 *
 */

public class DrinkLinkApplication extends Application {

    private static final String TAG = "DrinkLinkApplication";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static ApplicationComponent component;
    private static final Object LOCK = new Object();

    public static void reset(Context context) {
        component = null;
        getComponent(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate");

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/NunitoSans-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this.getApplicationContext());
        }

        patch();

        // Explicit initialization of Crashlytics is no longer required.

        ApplicationComponent component = getComponent(getApplicationContext());
        component.getNotificationsTokenUpdateService().subscribe();
        component.initV1Interceptor();

        initChannelId();
    }

    private void patch() {
        PatchManager patchManager = getUpdateComponent().getPatchManager();
        patchManager.patch(getCacheDir());
    }

    @NotNull
    private UpdateComponent getUpdateComponent() {
        return DaggerUpdateComponent.builder().updateModule(new UpdateModule(getApplicationContext())).build();
    }

    private void initChannelId() {
        // Since android Oreo notification channel is needed. We need one for silent notifications, another or sound notifications
        createChannel(getString(R.string.notifications_channel_id), RingtoneManager.TYPE_NOTIFICATION);
        createChannel(getString(R.string.notifications_ring_channel_id), RingtoneManager.TYPE_RINGTONE);
        createChannel(getString(R.string.notifications_silent_channel_id), null);

    }

    private void createChannel(String channelId, Integer tone) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            Logger.i(TAG, "before create " + channel);
            if (channel == null) {
                channel = new NotificationChannel(channelId, "DrinkLink notifications", NotificationManager.IMPORTANCE_HIGH);
                //enableLights(), setLightColor(), and setVibrationPattern()
                channel.setDescription("Updates about DrinkLink Orders");
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                channel.enableVibration(true);
                if (tone == null) {
                    channel.setSound(null, null);
                } else {
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();
                    channel.setSound(RingtoneManager.getDefaultUri(tone), audioAttributes);
                }
                notificationManager.createNotificationChannel(channel);
                channel = notificationManager.getNotificationChannel(channelId);
                Logger.i(TAG, "after create " + channel.getId());
            }
        }
    }


    public static ApplicationComponent getComponent(Context context) {
        if (component == null) {
            synchronized (LOCK) {
                if (component == null) {
                    component = DaggerApplicationComponent.builder()
                            .applicationModule(new ApplicationModule(context)).build();
                }
            }
        }
        return component;
    }

    public static ApplicationComponent getComponent() {
        return component;
    }

    public static String getResString(int id) {
        return component.getContext().getString(id);
    }


}
