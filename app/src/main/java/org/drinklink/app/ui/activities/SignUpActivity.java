/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.SingleFragmentToolbarActivity;
import org.drinklink.app.ui.fragments.SignUpFragment;

/**
 * Main activity called after order is finished
 */
public class SignUpActivity extends SingleFragmentToolbarActivity {

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return SignUpFragment.class;
    }
}