package org.drinklink.app.common.viewmodel;

import android.view.View;

/**
 *
 */

public class ViewModelClickableItem extends ViewModelItem {

    private View.OnClickListener onClick;

    public ViewModelClickableItem(int layoutResource, View.OnClickListener onClick) {
        super(layoutResource);
        this.onClick = onClick;
    }

    public View.OnClickListener getOnClick() {
        return onClick;
    }
}
