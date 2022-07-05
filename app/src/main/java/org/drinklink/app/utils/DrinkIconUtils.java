package org.drinklink.app.utils;

import android.content.Context;

import org.drinklink.app.ui.viewmodel.DrinkCategoryItem;

/**
 *
 */

public class DrinkIconUtils {

    public static int getDrinkIcon(Context ctx, DrinkCategoryItem item) {
        int iconIdInt = item.getCategory().getIconId();
        return getDrinkIcon(ctx, iconIdInt);
    }

    public static int getDrinkIcon(Context ctx, int iconIdInt) {
        iconIdInt = iconIdInt == 0 ? 3 : iconIdInt; // 3 as default if icon not set
        String iconId = Integer.toString(iconIdInt);
        return ctx.getResources().getIdentifier("ico_drink" + iconId, "drawable", ctx.getPackageName());
    }
}
