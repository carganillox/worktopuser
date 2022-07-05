package org.drinklink.app.common.contract;

import android.view.View;

/**
 * Created on 9/23/17.
 */

public abstract class ListItemClick<T> {

    public void clicked(View view) {
        T item= (T) view.getTag();
        process(item);
    }

    protected abstract void process(T item);
}
