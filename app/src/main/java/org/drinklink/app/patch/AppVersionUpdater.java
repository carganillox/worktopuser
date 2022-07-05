/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.patch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.drinklink.app.utils.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.AccessLevel;
import lombok.Getter;

/**
 *
 */
public class AppVersionUpdater {

    private static final String TAG = "AppVersionUpdater";

    public static String PREFERENCES_FILE = "org.drinklink.app.version.xml";

    private static final String VERSION = "version";

    private SharedPreferences sharedPreferences;
    @Getter(AccessLevel.PRIVATE)
    private Context context;

    @Inject
    public AppVersionUpdater(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        this.context = context;
    }

    public Integer getNewVersion() {
        int version = getVersion();
        int lastVersion = sharedPreferences.getInt(VERSION, -1);
        Logger.i(TAG, "last version:" + lastVersion);
        if (version > lastVersion) {
            return version;
        }
        return null;
    }

    private void persist(int version) {
        sharedPreferences.edit().putInt(VERSION, version).commit();
        Logger.i(TAG, "version persisted:" + version);
    }

    private int getVersion() {
        int version = -1;
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            version = pInfo.versionCode;
            Logger.i(TAG, "package version:" + version);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
        return version;
    }

    // this is explicit method because updates can clear all the data so version need to be explicitly persisted after that
    public void persistVersion() {
        persist(getVersion());
    }
}
