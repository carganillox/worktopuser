/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.dependency;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class UpdateModule {

    private Context context;

    public UpdateModule(Context context) {
        this.context = context;
    }

    @Provides
    Context providesContext(){
        return context;
    }
}
