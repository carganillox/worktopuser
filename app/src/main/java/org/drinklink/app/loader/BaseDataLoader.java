/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.loader;

import org.drinklink.app.api.ApiService;
import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.dependency.DependencyResolver;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */

public abstract class BaseDataLoader {

    @Inject
    @Named(ApplicationModule.API_SERVICE_CACHE)
    ApiService apiService;

    public BaseDataLoader() {
        DependencyResolver.getComponent().inject(this);
    }

    public String loadMore() {
        return null;
    }
}
