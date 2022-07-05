/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.SingleFragmentToolbarActivity;
import org.drinklink.app.ui.fragments.SignInFragment;

/**
 * Main activity called after order is finished
 */
public class SignInActivity extends SingleFragmentToolbarActivity {

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return SignInFragment.class;
    }
}