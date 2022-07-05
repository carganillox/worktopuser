package org.drinklink.app.common.viewmodel;

import android.util.Pair;

import java.util.Arrays;
import java.util.List;

/**
 *
 */

public class TabSelectorItem {

    private List<Pair<String, Integer>> tabDescription;

    public TabSelectorItem(Pair<String, Integer>... tabDescription) {
        this.tabDescription = Arrays.asList(tabDescription);
    }

    public List<Pair<String, Integer>> getTabDescription() {
        return tabDescription;
    }
}
