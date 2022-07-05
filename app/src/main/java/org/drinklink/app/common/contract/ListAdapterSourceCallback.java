package org.drinklink.app.common.contract;

public interface ListAdapterSourceCallback<T> {
        void onNext(T data, String callToken);
        void onCompleted();
        void onError(Error error, String callToken);
}