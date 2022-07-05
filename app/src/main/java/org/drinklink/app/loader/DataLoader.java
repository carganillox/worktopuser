/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import android.app.Activity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.utils.Logger;

import java.util.HashSet;

import lombok.NoArgsConstructor;
import rx.Observable;
import rx.Subscription;

/**
 *
 */
@NoArgsConstructor
public abstract class DataLoader<T> extends BaseDataLoader {

    private static String TAG = "DataLoader";

    public DataLoader(Activity activity) {
        this.activity = activity;
    }

    private HashSet<Subscription> subscriptions = new HashSet<>();
    private Observable<T> observable;
    protected Activity activity;

    public DataLoader<T> load(final ListAdapterSourceCallback<T> callback, final boolean forceRefresh) {
        Logger.i(TAG, "load");
        observable = getObservable(forceRefresh);
        Subscription subscribe = observable.subscribe(new LoaderListSubscriber<T>(callback) {
            @Override
            public void onError(Throwable e) {
                Logger.e(TAG, "load error, show retry dialog", e);
                FirebaseCrashlytics.getInstance().recordException(e);

                if (activity != null) {
                    DialogManager.showYesNoDialog(activity,
                            activity.getString(R.string.error_an_error),
                            activity.getString(R.string.error_ok_to_retry),
                            () -> {
                                apiService.reset();
                                Logger.i(TAG, "load again");
                                load(callback, true);
                            }, () -> super.onError(e));
                } else {
                    super.onError(e);
                }
            }
        });
        synchronized (subscriptions) {
            subscriptions.add(subscribe);
        }
        return this;
    }

    public void unSubscribe() {
        Logger.i(TAG, "unsubscribe loader");
        synchronized (subscriptions) {
            for(Subscription subscription : subscriptions) {
                if (!subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
            subscriptions.clear();
        }
    }

    protected ApiService getApiService() {
        return apiService;
    }

    protected Observable<T> getObservable() {
        return observable;
    }

    protected abstract Observable<T> getObservable(boolean forceRefresh);
}
