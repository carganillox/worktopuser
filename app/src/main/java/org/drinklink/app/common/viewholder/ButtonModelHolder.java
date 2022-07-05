package org.drinklink.app.common.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.drinklink.app.common.viewmodel.ButtonModelItem;

/**
 *
 */

public class ButtonModelHolder extends ViewModelBaseHolder<ButtonModelItem> {

    public ButtonModelHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, ButtonModelItem item) {
        super.bind(ctx, position, item);
        ((Button)itemView).setText(item.getText());
        itemView.setOnClickListener(item.getOnClick());
    }
}
