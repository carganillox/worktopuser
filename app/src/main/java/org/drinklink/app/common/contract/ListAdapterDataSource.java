/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.contract;

import org.drinklink.app.loader.DataLoader;
import org.drinklink.app.utils.Logger;

/**
 *
 */

public class ListAdapterDataSource<T> implements ListAdapterSource {

    private static final String TAG = "ListAdapterDataSource";

    DataLoader<T> loader;
    ListAdapterSourceCallback<T> callback;

    public ListAdapterDataSource(ListAdapterSourceCallback<T> callback, DataLoader<T> loader) {
        this.loader = loader;
        this.callback = callback;
    }

    @Override
    public String onRefresh(boolean forceRefresh) {
        Logger.i(TAG, "Calling onRefresh");
        loader.load(callback, forceRefresh);
        return null;
    }

    @Override
    public void unSubscribe() {
        Logger.i(TAG, "Calling unSubscribe");
        loader.unSubscribe();
    }

    @Override
    public String loadMoreData() {
        Logger.i(TAG, "Calling loadMoreData");
        return loader.loadMore();
    }
}
