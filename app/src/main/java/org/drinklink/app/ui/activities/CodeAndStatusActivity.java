/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.SingleFragmentToolbarActivity;
import org.drinklink.app.ui.fragments.CodeAndStatusFragment;

/**
 *
 */

public class CodeAndStatusActivity extends SingleFragmentToolbarActivity {

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return CodeAndStatusFragment.class;
    }
}
