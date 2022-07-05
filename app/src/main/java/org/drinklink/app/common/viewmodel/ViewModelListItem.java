package org.drinklink.app.common.viewmodel;

import java.util.List;

/**
 *
 */

public class ViewModelListItem<T> extends ViewModelItem {

    private List<T> data;

    public ViewModelListItem(List<T> data, int emptyResId) {
        this.data = data;
        this.layoutResource = emptyResId;
    }

    @Override
    public int getViewByResource() {
        return isEmpty() ? layoutResource : -1;
    }

    private boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    public List<T> getData() {
        return data;
    }

    public Class getModelClass() {
        return isEmpty() ? this.getClass() : data.get(0).getClass();
    }
}
