package org.drinklink.app.utils;

import android.content.Intent;

/**
 *
 */
public class IntentUtils {

    public static int OVER_EXISTING = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

    public static int CLEAR_AND_NEW = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK;

    public static int NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK;
}
