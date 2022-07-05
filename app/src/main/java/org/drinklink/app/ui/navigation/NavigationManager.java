/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.navigation;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

/**
 *
 */

public interface NavigationManager {

    Fragment next(Class<? extends Fragment> fragmentClass, Bundle extras, boolean addToBackStack);

    String loadFragment(Fragment fragment, boolean addToBackStack, Bundle extras);

    void previous(String tag);

    boolean hasFragments();

    void toFirst(Class<? extends Fragment> fragmentClass);
}
