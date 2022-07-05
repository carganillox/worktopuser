package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class ViewModelItem {

    protected int layoutResource = -1;

    public ViewModelItem() {
    }

    public ViewModelItem(int layoutResource) {
        this.layoutResource = layoutResource;
    }

    public int getViewByResource() {
        return this.layoutResource;
    }
}
