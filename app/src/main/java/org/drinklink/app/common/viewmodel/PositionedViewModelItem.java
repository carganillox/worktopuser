package org.drinklink.app.common.viewmodel;

import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */

public class PositionedViewModelItem<T> extends ViewModelItemWrapper<T> {

    private static final String TAG = "PositionedViewModelItem";

    private boolean isLast;
    private int position;

    //IMPORTANT: do not remove, used for newInstance
    public PositionedViewModelItem() {
    }

    public boolean isLast() {
        return isLast;
    }

    public int getPosition() {
        return position;
    }

    public static <T, S extends PositionedViewModelItem<T>> List<S> asPositionedList(T[] items, Class<S> clazz) {
        if (items == null) {
            return new ArrayList<>();
        }
        return asPositionedList(Arrays.asList(items), clazz);

    }
    public static <T, S extends PositionedViewModelItem<T>> List<S> asPositionedList(Collection<T> items, Class<S> clazz) {

        if (items == null) {
            return new ArrayList<>();
        }
        int size = items.size();
        int lastPosition = size - 1;
        List<S> positioned = new ArrayList<>(size);

        int counter = 0;
        for(T item : items) {
            S inst = null;
            try {
                inst = clazz.newInstance();
                inst.set(item, counter == lastPosition, counter);
            } catch (InstantiationException e) {
                logError(clazz, e);
            } catch (IllegalAccessException e) {
                logError(clazz, e);
            }
            positioned.add(inst);
            counter ++;
        }
        return positioned;
    }

    public void set(T subject, boolean isLast, int position) {
        this.item = subject;
        this.isLast = isLast;
        this.position = position;
    }

    private static void logError(Class clazz, Exception e) {
        Logger.e(TAG, "Constructor error" + clazz.getSimpleName(), e);
    }
}
