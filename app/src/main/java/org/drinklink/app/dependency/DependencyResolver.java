/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.dependency;

import android.content.Context;

import org.drinklink.app.DrinkLinkApplication;

/**
 *
 */

public class DependencyResolver {
    private DependencyResolver() {
    }

    public static ApplicationComponent getComponent() {
        return DrinkLinkApplication.getComponent();
    }

    public static ApplicationComponent getComponent(Context context) {
        return DrinkLinkApplication.getComponent(context);
    }

    public static String getResString(int resId) {
        return DrinkLinkApplication.getResString(resId);
    }

    public static void reset(Context context) {
        DrinkLinkApplication.reset(context);
    }
}
