/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

/**
 * based on : https://gist.github.com/mttkay/24881a0ce986f6ec4b4d
 */

public abstract class PagedLoader<T> extends DataLoader<T> {

    private PublishSubject<Observable<List<T>>> pages;
    private Subscription subscription = Subscriptions.empty();
    private int pageIndex = 0;
    private boolean forceRefresh;

    @Override
    protected Observable<T> getObservable(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;

        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                pages = PublishSubject.create();
                subscription = Observable.switchOnNext(pages).subscribe(new PageSubscriber(subscriber));
                subscriber.add(subscription);
                pages.onNext(getFlatPage(forceRefresh, pageIndex));
            }
        });


//        pages = PublishSubject.create();
//        pages.doOnNext(p -> {
//            if (p == null) {
//                pages.onCompleted();
//            }
//        });
//        pageIndex = 0;
//        placeObservable = pages.startWith(getFlatPage(this.forceRefresh, pageIndex))
//                .doOnNext(page -> getFlatPage(this.forceRefresh, ++pageIndex));
//        return placeObservable;
    }

    protected abstract Observable<List<T>> getObservablePage(boolean forceRefresh, int page);

    private Observable<List<T>> getFlatPage(boolean forceRefresh, int page) {
        return getObservablePage(forceRefresh, page);
    }


    @Override
    public String loadMore() {
        if (pages.hasObservers()) {
            pages.onNext(getFlatPage(this.forceRefresh, ++pageIndex));
        }
        return null;
    }

    private final class PageSubscriber extends Subscriber<List<T>> {
        private final Subscriber<? super T> inner;

        public PageSubscriber(Subscriber<? super T> inner) {
            this.inner = inner;
        }

        @Override
        public void onCompleted() {
            inner.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            inner.onError(e);
        }

        @Override
        public void onNext(List<T> result) {
            for (T item : result) {
                inner.onNext(item);
            }
            if(result.isEmpty()) {
                pages.onCompleted();
                inner.onCompleted();
            }
        }
    }
}
