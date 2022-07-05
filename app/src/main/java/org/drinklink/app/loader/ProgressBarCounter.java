/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.drinklink.app.utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.Getter;

public class ProgressBarCounter {

    private static final String TAG = "ProgressBarCounter";

    public static final ProgressBarCounter NO_PROGRESS_BAR = new ProgressBarCounter(null) {
        @Override
        public void increase() {

        }

        @Override
        public void decrease() {

        }
    };

    @Getter(AccessLevel.PRIVATE)
    private Activity activity;

    private ProgressBar progressBar;

    public ProgressBarCounter(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ProgressBarCounter(ProgressBar progressBar, Activity activity) {
        this.progressBar = progressBar;
        this.activity = activity;
    }

    private AtomicInteger counter = new AtomicInteger(0);

    public void increase() {
        int i = counter.incrementAndGet();
        if (i == 1 && progressBar != null) {
            Logger.d(TAG, "VISIBLE");
            progressBar.setVisibility(View.VISIBLE);
            if (getActivity()  != null) {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }

    public void decrease() {
        int i = counter.decrementAndGet();
        if (i == 0 && progressBar != null) {
            Logger.d(TAG, "GONE");
            progressBar.setVisibility(View.GONE);
            if (getActivity()  != null) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }

    public boolean isWorking() {
        return counter.get() > 0;
    }

}