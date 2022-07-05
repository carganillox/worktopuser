package org.drinklink.app.utils;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class StringUtils {

    private StringUtils() {
    }

    @NotNull
    public static String getSubstring(String s, int i, int i2) {
        return s == null || s.length() < i2 ? null : s.substring(i, i2);
    }
}
