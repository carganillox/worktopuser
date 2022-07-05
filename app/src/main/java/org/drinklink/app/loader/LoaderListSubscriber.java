/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import org.drinklink.app.R;
import org.drinklink.app.common.contract.Error;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.utils.Logger;

import rx.Subscriber;

/**
 *
 */

public class LoaderListSubscriber<T> extends Subscriber<T> {

    private static final String TAG = "LoaderListSubscriber";

    private ListAdapterSourceCallback<T> sourceCallback;

    public LoaderListSubscriber(ListAdapterSourceCallback<T> sourceCallback) {
        this.sourceCallback = sourceCallback;
    }

    @Override
    public void onCompleted() {
        Logger.d(TAG, "Callback completed");
        sourceCallback.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        Logger.e(TAG, "Callback error, " + e.getMessage(), e);
        Error error = new Error(DependencyResolver.getResString(R.string.error_api_network));
        sourceCallback.onError(error, null);
    }

    @Override
    public void onNext(T places) {
        Logger.d(TAG, "Callback success");
        sourceCallback.onNext(places, null);
    }
}
