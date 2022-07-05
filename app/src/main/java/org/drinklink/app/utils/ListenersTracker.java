/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import org.drinklink.app.common.viewholder.UpdateFunction;
import org.drinklink.app.common.viewholder.UpdateSingleFunction;

import java.util.ArrayList;

/**
 *
 */

public abstract class ListenersTracker<T, S> {

    private ArrayList<T> listeners = new ArrayList<>();

    public void registerListener(T listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void unRegisterListener(T listener) {
        synchronized (listeners) {
            if (listener == null) {
                listeners.clear();
            } else {
                listeners.remove(listener);
            }
        }
    }

    public void notifyListeners(UpdateSingleFunction<T, Void> action) {
        ArrayList<T> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<>(this.listeners);
        }
        for(T listener : listenersCopy) {
            action.apply(listener);
        }
    }

    public void notifyListeners(S item, UpdateFunction<T, S, Void> action) {
        ArrayList<T> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<>(this.listeners);
        }
        for(T listener : listenersCopy) {
            action.apply(listener, item);
        }
    }

    protected void call(T listener) {}

}
