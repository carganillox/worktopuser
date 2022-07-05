package org.drinklink.app.utils;

import org.drinklink.app.model.request.OrderItemRequest;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SortUtils {

    public static List<OrderItemRequest> sort(List<OrderItemRequest> list) {
        Collections.sort(list, (sel1, sel2) -> {
            long t1 = sel1.getTimestamp();
            long t2 = sel2.getTimestamp();
            long delta = t1 - t2;
            return delta > 0 ? 1 : -1;
        });
        return list;
    }
}
