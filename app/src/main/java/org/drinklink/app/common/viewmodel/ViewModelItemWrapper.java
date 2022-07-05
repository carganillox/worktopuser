package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class ViewModelItemWrapper<T> {

    protected T item;

    public ViewModelItemWrapper() {
    }

    public ViewModelItemWrapper(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }
}
