package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class IconHeaderModelItem extends ViewModelItem {

    private final String icon;
    private final String text;

    public IconHeaderModelItem(String text, String icon) {
        this.text = text;
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
