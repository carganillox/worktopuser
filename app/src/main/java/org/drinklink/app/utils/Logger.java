/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */

public class Logger {

    private static String APP_TAG = "DrinkLink:";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static LimitedQueue<String> queue = new LimitedQueue<>(1000);

    private static void track(Class clazz, String message) {
        track(clazz.getSimpleName(), getClazz(message));
    }

    private static void track(String clazz, String message) {
        queue.add(sdf.format(System.currentTimeMillis())  + ": " + getClazz(clazz) + " : " + getClazz(message) + "\n");
    }

    @Deprecated
    public static void e(Class clazz, String message, Throwable e) {
        Log.e(clazz.getSimpleName(), getClazz(message), e);
    }

    @Deprecated
    public static void e(Class clazz, String message) {
        track(clazz, getClazz(message));
        Log.e(clazz.getSimpleName(), message);
    }

    @Deprecated
    public static void d(Class clazz, String message) {
        track(clazz, getClazz(message));
        Log.d(clazz.getSimpleName(), message);
    }

    @Deprecated
    public static void i(Class clazz, String message) {
        track(clazz, getClazz(message));
        Log.i(clazz.getSimpleName(), message);
    }

    @Deprecated
    public static void v(Class clazz, String message) {
        track(clazz, getClazz(message));
        Log.v(clazz.getSimpleName(), message);
    }

    @Deprecated
    public static void w(Class clazz, String message) {
        track(clazz, getClazz(message));
        Log.w(clazz.getSimpleName(), message);
    }

    public static void e(String clazz, String message, Throwable e) {
        Log.e(getClazz(clazz), message, e);
    }

    public static void e(String clazz, String message) {
        track(getClazz(clazz), message);
        Log.e(getClazz(clazz), message);
    }

    public static void d(String clazz, String message) {
        track(getClazz(clazz), message);
        Log.d(getClazz(clazz), message);
    }

    public static void i(String clazz, String message) {
        track(getClazz(clazz), message);
        Log.i(getClazz(clazz), message);
    }

    private static String getClazz(String clazz) {
        return APP_TAG + clazz;
    }

    public static void v(String clazz, String message) {
        track(getClazz(clazz), message);
        Log.v(getClazz(clazz), message);
    }

    public static void w(String clazz, String message) {
        track(getClazz(clazz), message);
        Log.w(getClazz(clazz), message);
    }

    public static List<String> dumpLogs() {
        ArrayList<String> dump = new ArrayList<>(queue);
        queue.clear();
        return dump;

    }

    public static class LimitedQueue<E> extends LinkedList<E> {

        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            boolean added = super.add(o);
            while (added && size() > limit) {
                super.remove();
            }
            return added;
        }
    }
}
