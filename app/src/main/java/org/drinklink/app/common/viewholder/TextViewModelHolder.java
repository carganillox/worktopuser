package org.drinklink.app.common.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.drinklink.app.common.viewmodel.TextViewModelItem;

/**
 *
 */

public class TextViewModelHolder extends ViewModelBaseHolder<TextViewModelItem> {

    public TextViewModelHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, TextViewModelItem viewModelItem) {
        super.bind(ctx, position, viewModelItem);
        ((TextView)itemView).setText(viewModelItem.getText());
    }
}
