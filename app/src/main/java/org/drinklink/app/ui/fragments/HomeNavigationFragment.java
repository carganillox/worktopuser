/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import org.drinklink.app.R;

import butterknife.OnClick;

/**
 *
 */

public class HomeNavigationFragment extends NavigationFragment {

    @Override
    @OnClick(R.id.menu_home)
    public void onHome() {
        getNavigation().toFirst(CategoriesListFragment.class);
        closeDrawer();
    }
}
