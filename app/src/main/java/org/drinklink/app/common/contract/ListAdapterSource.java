package org.drinklink.app.common.contract;

public interface ListAdapterSource {

    String onRefresh(boolean forceRefresh);

    String loadMoreData();

    void unSubscribe();
}

