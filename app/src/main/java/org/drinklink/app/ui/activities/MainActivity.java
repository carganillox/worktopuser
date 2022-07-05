/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.drinklink.app.R;
import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.ui.fragments.CategoriesListFragment;
import org.drinklink.app.ui.fragments.HomeNavigationFragment;
import org.drinklink.app.ui.fragments.NavigationFragment;
import org.drinklink.app.ui.fragments.OrderStatusFragment;
import org.drinklink.app.ui.fragments.PlaceListSearchFragment;
import org.drinklink.app.ui.navigation.NavigationManagerImpl;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

public class MainActivity extends ToolbarActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void loadFragment() {
        if (!getNavigation().hasFragments()) {
            Class<? extends Fragment> fragmentExtra = getFragmentExtra();
            boolean showStatus = fragmentExtra.equals(OrderStatusFragment.class);
            boolean isFirst = fragmentExtra.equals(PlaceListSearchFragment.class);
            //fragmentExtra = CategoriesListFragment.class;
            // extras are potentially updated in getFragmentExtra
            Bundle extras = getBundle();
            Logger.d(TAG, "loadFragment: " + fragmentExtra);
            if (showStatus) {
                Intent intent = new Intent(getApplicationContext(), OrderStatusActivity.class);
                intent.setFlags(IntentUtils.CLEAR_AND_NEW);
                prepareOrderStateIntent(getProcessor(), intent);
                startActivity(intent);
            } else {
                contentFragment = getNavigation().next(fragmentExtra, extras, !isFirst && addToBackStack());
            }
        }
    }

    @Override
    protected Class<? extends Fragment> getFragmentExtra() {

        IOrderProcessor processor = getProcessor();
        if (hasPlaceIdExtra()) {
            return showCategories();
        } else if (showStatus() && processor.isActive()) {
            Logger.i(TAG, "navigate to OrderStatusFragment");
            return OrderStatusFragment.class;
        }
        Logger.i(TAG, "navigate to PlaceListSearchFragment");
        return PlaceListSearchFragment.class;
    }

    private boolean hasPlaceIdExtra() {
        return getIntent().getIntExtra(ExtrasKey.PLACE_ID_EXTRA, -1) != -1;
    }

    private void prepareOrderStateIntent(IOrderProcessor orderProcessor, Intent intent) {
        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, orderProcessor.getActiveOrderId());
        intent.putExtra(ExtrasKey.PLACE_EXTRA, getGson().toJson(orderProcessor.getActiveOrderPlace()));
    }

    protected boolean showStatus() {
        return getIntent().getBooleanExtra(ExtrasKey.SHOW_STATUS, true);
    }

    @NonNull
    private Class<? extends Fragment> showCategories() {
        // intent already has place extra
        Logger.i(TAG, "navigate to CategoriesListFragment");
        return CategoriesListFragment.class;
    }

    @NonNull
    @Override
    protected NavigationManagerImpl createNavigation() {
        return new NavigationManagerImpl(this, R.id.container, () -> displayBackArrow(getNavigation().hasFragments()));
    }

    @NonNull
    @Override
    protected Class<? extends NavigationFragment> getNavigationFragment() {
        return HomeNavigationFragment.class;
    }
}