/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.activity;

/**
 *
 */
public class SingleFragmentToolbarActivity extends NoSettingsToolbarActivity {


    @Override
    protected boolean displayBackArrow() {
        return true;
    }

    @Override
    protected boolean addToBackStack() {
        return false;
    }
}
