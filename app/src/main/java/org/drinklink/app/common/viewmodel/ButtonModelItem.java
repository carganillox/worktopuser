package org.drinklink.app.common.viewmodel;

import android.view.View;

/**
 *
 */

public class ButtonModelItem extends ViewModelItem {

    private final String text;
    private final View.OnClickListener onClick;

    public ButtonModelItem(int layoutResource, String text, View.OnClickListener onClick) {
        super(layoutResource);
        this.text = text;
        this.onClick = onClick;
    }

    public String getText() {
        return text;
    }

    public View.OnClickListener getOnClick() {
        return onClick;
    }
}
