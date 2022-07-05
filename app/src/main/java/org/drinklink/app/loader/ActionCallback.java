/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.drinklink.app.R;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.ui.activities.SignInActivity;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;

/**
 *
 */

public abstract class ActionCallback<T> implements retrofit2.Callback<T> {

    private static final String TAG = "ActionCallback";

    public static final int INTERNAL_ERROR = 0;
    @Getter
    private final ProgressBarCounter progressBar;

    private boolean isPaused = false;
    protected boolean delayedResponse = false;
    private T body;
    private boolean delayedError;
    private int code;
    private String message;
    private String errorBody;
    private AtomicBoolean isProgressBar;
    private Context ctx;
    private boolean executed;

    protected abstract void onError(int code, String message, String errorBody);

    public ActionCallback(ProgressBarCounter progressBar, Context ctx) {
        this.progressBar = progressBar;
        progressBar.increase();
        isProgressBar = new AtomicBoolean(false);
        this.ctx = ctx;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        Logger.i(TAG + hashCode(), "Response, isResumed: " + !isPaused + ", success: " + response.isSuccessful() +
                ", url:" + call.request().url() + ", verb: " + call.request().method());
        if (response.isSuccessful()) {
            if (!isPaused) {
                Logger.i(TAG, "execute success");
                executeOnSuccess(response.body());
            } else {
                Logger.i(TAG, "delay success");
                delayedResponse = executeOnResume();
                this.body = response.body();
            }
        } else {
            String errorBody = getErrorMessage(response);
            if (!isPaused) {
                Logger.i(TAG, "execute error");
                executeOnError(response.code(), response.message(), errorBody);
            } else {
                Logger.i(TAG, "delay error");
                delayedError = executeOnResume();
                this.code = response.code();
                this.message = response.message();
                this.errorBody = errorBody;
            }
        }
    }

    @Nullable
    private String getErrorMessage(Response<T> response) {
        String errorBody = null;
        try {
            errorBody = response.errorBody().string();
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
        return errorBody;
    }

    private void decreaseProgressBar() {
        if (isProgressBar.compareAndSet(false, true)) {
            progressBar.decrease();
        }
    }

    protected boolean executeOnResume() {
        return true;
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        FirebaseCrashlytics.getInstance().recordException(t);
        Logger.e(TAG, t.getMessage(), t);
        int errorCode = INTERNAL_ERROR;
        if (t instanceof HttpException) {
            errorCode = ((HttpException)t).code();
        }
        executeOnError(errorCode, getAnError(), null);
    }

    private String getAnError() {
         return DependencyResolver.getResString(R.string.error_an_error);
    }

    protected void executeOnSuccess(T body) {
        executed = true;
        decreaseProgressBar();
        onSuccess(body);
    }

    public void onSuccess(T body) {
    }

    public void executeOnError(int code, String message, String errorBody) {
        executed = true;
        decreaseProgressBar();
        if (code == 401 && ctx != null) {
            Intent intent = new Intent(ctx, SignInActivity.class);
            intent.setFlags(IntentUtils.OVER_EXISTING);
            ctx.startActivity(intent);
        }
        if (!TextUtils.isEmpty(errorBody)) {
            errorBody = fixErrorBody(errorBody);
        }
        onError(code, message, errorBody);
    }

    @NotNull
    public static String fixErrorBody(String errorBody) {
        String s = errorBody
                .replaceAll("\\r\\n|\\r|\\n", " ");
        String s1 = s.replaceAll("^\"|\"$", "");
        String s2 = s1.replaceAll(String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
        String s3 = s2.replaceAll(" +", " ");
        String s4 = s3.replaceAll(" \\.", ".");
        String s5 = s4.replaceAll("\\s+(?=[),])", "");
        String s6 = s5.replace("\\r \\n", "")
                .replaceAll("' A '", "'A'")
                .replaceAll(" :", ":")
                .replaceAll("' Z '", "'Z'");
        String fixed = s6;
        if (fixed.endsWith("\\r \\n")) {
            fixed = fixed.substring(0, fixed.length() - 5);
        }
        if (fixed.endsWith("\\r\\n")) {
            fixed = fixed.substring(0, fixed.length() - 4);
        }
        return fixed;
    }

    /**
     * @return return if callback is already executed
     */
    public boolean onPause() {
        Logger.i(TAG + hashCode(), "onPaused");
        isPaused = true;
        return executed;
    }

    /**
     * @return return if callback is already executed
     */
    public boolean onResume() {
        Logger.i(TAG + hashCode(), "onResume, isDelayedResponse: " + delayedResponse + ", isDelayedError: " + delayedError + ", isExecuted: " + executed);
        isPaused = false;
        if (delayedResponse) {
            executeOnSuccess(this.body);
            delayedResponse = false;
        }
        if (delayedError) {
            delayedError = false;
            executeOnError(code, message, errorBody);
        }
        return executed;
    }

}

