/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.ui.fragments.DrinksListFragment;

/**
 *
 */
@Deprecated
public class DrinkMenuActivity extends ToolbarActivity {

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return DrinksListFragment.class;
    }

    @Override
    protected boolean displayBackArrow() {
        return true;
    }
}
