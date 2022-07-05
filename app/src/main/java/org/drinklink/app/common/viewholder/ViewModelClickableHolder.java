package org.drinklink.app.common.viewholder;

import android.view.View;
import android.widget.Button;

/**
 *
 */

public class ViewModelClickableHolder<T> extends ViewModelBaseHolder<T> {

    protected final View.OnClickListener onClick;

    public ViewModelClickableHolder(View itemView, View.OnClickListener onClick) {
        super(itemView);
        this.onClick = onClick;
    }

    protected void setButtonLink(Button btn, String btnText, String btnLink) {
        setButtonLink(btn, btnText, btnLink, onClick);
    }
}
