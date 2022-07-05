package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class SectionModel extends ViewModelItem {

    private String title;

    public SectionModel(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
