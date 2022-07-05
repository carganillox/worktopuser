/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.navigation;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.ui.activities.MainActivity;
import org.drinklink.app.ui.fragments.CategoriesListFragment;
import org.drinklink.app.ui.fragments.PaymentFragment;
import org.drinklink.app.ui.fragments.PlaceListSearchFragment;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;

/**
 *
 */

public class NavigationManagerImpl implements NavigationManager {

    private static final String TAG = "NavigationManagerImpl";

    private static final ArrayList<Class<? extends Fragment>> FRAGMENTS = new ArrayList<>();

    static {
        FRAGMENTS.add(PlaceListSearchFragment.class);
        FRAGMENTS.add(CategoriesListFragment.class);
        FRAGMENTS.add(PaymentFragment.class);
    }

    @IdRes
    private final int containerId;
    private final FragmentActivity fragmentActivity;

    public NavigationManagerImpl(FragmentActivity fragmentActivity, int container) {
        this.fragmentActivity = fragmentActivity;
        this.containerId = container;
    }

    public NavigationManagerImpl(MainActivity mainActivity, int container, Runnable runnable) {
        this(mainActivity, container);
        getSupport().addOnBackStackChangedListener(() -> {
            Logger.d(TAG, "backStackEntryCount: " + getSupport().getBackStackEntryCount());
            runnable.run();
        });
    }

    @Override
    public Fragment next(Class<? extends Fragment> fragmentClass, Bundle extras, boolean addToBackStack) {
        Fragment fragment = getFragmentForClass(fragmentClass);
        loadFragment(fragment, addToBackStack, extras);
        initFragment(fragment, extras);
        return fragment;
    }

    @Override
    public void previous(String tag) {
        while (!isMatchingTag(tag)) {
            getSupport().popBackStackImmediate();
        }
    }

    @Override
    public void toFirst(Class<? extends Fragment> fragmentClass) {
        int count = getSupport().getBackStackEntryCount();
        String start = fragmentClass.getCanonicalName() + ":";
        boolean startClearing = false;
        for (int i = 0; i < count; i ++) {
            if (startClearing) {
                getSupport().popBackStackImmediate();
            } else {
                FragmentManager.BackStackEntry backEntry = getSupport().getBackStackEntryAt(i);
                if (backEntry.getName().startsWith(start)) {
                    startClearing = true;
                    continue;
                }
            }
        }

    }

    private boolean isMatchingTag(String tag) {
        int index = getSupport().getBackStackEntryCount() - 1;
        FragmentManager.BackStackEntry backEntry = getSupport().getBackStackEntryAt(index);
        String visibleFragmentTag = backEntry.getName();
        boolean isMatchingTag = visibleFragmentTag.equals(tag);
        Logger.i(TAG, isMatchingTag ?
                "matched tag" :
                "remove category tag: " + tag + "visible frag. tag: " + visibleFragmentTag);
        return isMatchingTag;
    }

    @Override
    public boolean hasFragments() {
        int backStackEntryCount = getBackStackEntryCount();
        return backStackEntryCount > 0;
    }

    private int getBackStackEntryCount() {
        FragmentManager sm = getSupport();
        return sm.getBackStackEntryCount();
    }

    public String loadFragment(Fragment fragment, boolean addToBackStack, Bundle extras) {
        if (fragment == null) {
            return null;
        }

        if (!hasFragments()) {
            populatePreviousFragments(fragment, extras);
        }
        FragmentTransaction transaction = getSupport().beginTransaction();
        if (hasFragments()) {
            //transaction.setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
        }
        transaction.replace(containerId, fragment);
        String tag = getTag(fragment);
        if (addToBackStack) {
            Logger.i(TAG, "Add tag: " + tag);
            transaction.addToBackStack(tag);
        }

        transaction.commit();
        return tag;
    }

    private void populatePreviousFragments(Fragment targetFragment, Bundle extras) {
        int index = FRAGMENTS.indexOf(targetFragment.getClass());
        if (index > 0) {
            for (int i = 0; i < index; i++) {
                FragmentTransaction transaction = getSupport().beginTransaction();
                Class<? extends Fragment> current = FRAGMENTS.get(i);
                Fragment currentFragment = getFragmentForClass(current);
                initFragment(currentFragment, extras);
                String tag = getTag(currentFragment);
                transaction.replace(containerId, currentFragment, tag);
                if (i > 0) {
                    transaction.addToBackStack(tag);
                }
                transaction.commit();
                Logger.i(TAG, "Added to back-stack: " + tag + " " + current.getSimpleName());
            }
        }
    }

    private String getTag(Fragment fragment) {
        return (fragment instanceof DrinkLinkFragment) ?
                getNavigationTag((DrinkLinkFragment) fragment) : fragment.getTag();
    }

    private <T extends DrinkLinkFragment> String getNavigationTag(T fragment) {
        return fragment.getNavigationTag();
    }

    private FragmentManager getSupport() {
        return fragmentActivity.getSupportFragmentManager();
    }

    private void initFragment(Fragment fragment, Bundle extras) {

        if (fragment instanceof DrinkLinkFragment) {
            DrinkLinkFragment dlFragment = (DrinkLinkFragment) fragment;
            dlFragment.init(extras);
            if (fragmentActivity instanceof ToolbarActivity) {
                dlFragment.setNavigation(((ToolbarActivity) fragmentActivity).getNavigation());
            }
        }
    }

    private Fragment getFragmentForClass(Class<? extends Fragment> fragmentClass) {
        if (fragmentClass == null) {
            return null;
        }
        try {
            Fragment fragment = fragmentClass.newInstance();
            return fragment;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.e(TAG, "create fragment " + fragmentClass, e);
        }
        return null;
    }
}
