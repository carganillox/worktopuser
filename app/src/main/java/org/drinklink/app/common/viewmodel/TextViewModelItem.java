package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class TextViewModelItem extends ViewModelItem {

    private String text;

    public TextViewModelItem(int layoutResource, String text) {
        super(layoutResource);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
