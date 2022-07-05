package org.drinklink.app.utils;

import java.math.BigDecimal;

/**
 *
 */

public class MoneyUtils {

    private static final String TAG = "MoneyUtils";
    private static final BigDecimal PERCENTAGE_100 = new BigDecimal(100);

    private MoneyUtils() {
    }

    public static BigDecimal round(BigDecimal value) {
        BigDecimal rounded = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        if (rounded.compareTo(value) != 0) {
            Logger.i(TAG, "rounded value: " + value + ", to: " + rounded);
        }
        return rounded;
    }

    public static boolean isWholeNumber(BigDecimal value) {
        return value !=null && value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean greaterThanZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean notZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) != 0; // not using equals as "0" is not equals "0.000"
    }

    public static BigDecimal toPercentageRounded(BigDecimal value, BigDecimal percentage) {
        return round(percentage.multiply(value).divide(PERCENTAGE_100));
    }

    public static BigDecimal applyDiscountNotRounded(BigDecimal value, BigDecimal percentage) {
        return PERCENTAGE_100.subtract(percentage).multiply(value).divide(PERCENTAGE_100);
    }

    public static BigDecimal applyServiceChargeRounded(BigDecimal value, BigDecimal percentage) {
        return round(PERCENTAGE_100.add(percentage).multiply(value).divide(PERCENTAGE_100));
    }
}
