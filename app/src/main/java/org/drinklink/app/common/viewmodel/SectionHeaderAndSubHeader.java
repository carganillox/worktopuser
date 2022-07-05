package org.drinklink.app.common.viewmodel;

/**
 *
 */

public class SectionHeaderAndSubHeader extends ViewModelItem {

    public String header;
    public String subheader;
    public SectionHeaderAndSubHeader(String header, String subheader) {
        this.subheader = subheader;
        this.header = header;
    }
}
