/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class GlideLoader {

    private RequestManager with;

    @Inject
    public GlideLoader(Context context) {
         with = Glide.with(context);
    }

    public RequestManager with() {
        return with;
    }
}
