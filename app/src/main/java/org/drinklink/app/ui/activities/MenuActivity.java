/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.ui.fragments.CategoriesListFragment;

/**
 *
 */
@Deprecated
public class MenuActivity extends ToolbarActivity {

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return CategoriesListFragment.class;
    }

    @Override
    protected boolean displayBackArrow() {
        return true;
    }
}
