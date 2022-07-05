/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.contract;

import org.drinklink.app.utils.Logger;

import java.util.Collection;

/**
 *
 */

public class ListAdapterStaticSource<T> implements ListAdapterSource {

    private static final String TAG = "ListAdapterStaticSource";

    private ListAdapterSourceCallback<T> callback;
    private Collection<T> data;

    public ListAdapterStaticSource(ListAdapterSourceCallback<T> callback, Collection<T> data) {
        this.callback = callback;
        this.data = data;
    }

    @Override
    public String onRefresh(boolean forceRefresh) {
        Logger.i(TAG, "Calling onRefresh");
        if (callback != null) {
            for (T item : data) {
                callback.onNext(item, null);
            }
            callback.onCompleted();
        }
        return null;
    }

    @Override
    public void unSubscribe() {
    }

    @Override
    public String loadMoreData() {
        return null;
    }
}
