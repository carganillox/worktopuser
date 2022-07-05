package org.drinklink.app.common.viewholder;

import android.content.Context;
import android.view.View;

import org.drinklink.app.common.viewmodel.ViewModelClickableItem;

/**
 *
 */

public class ViewModelClickableItemHolder extends ViewModelBaseHolder<ViewModelClickableItem> {

    public ViewModelClickableItemHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, ViewModelClickableItem item) {
        super.bind(ctx, position, item);
        setClickListenerWithTag(itemView, item, item.getOnClick());
    }
}
