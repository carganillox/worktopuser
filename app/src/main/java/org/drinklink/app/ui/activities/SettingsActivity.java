/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.drinklink.app.common.activity.SingleFragmentToolbarActivity;
import org.drinklink.app.ui.fragments.SettingsFragment;

/**
 * Main activity called after order is finished
 */
public class SettingsActivity extends SingleFragmentToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected Class<? extends Fragment> getFragmentExtra() {
        return SettingsFragment.class;
    }
}