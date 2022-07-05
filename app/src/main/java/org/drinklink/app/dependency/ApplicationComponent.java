/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.dependency;

import android.content.Context;

import org.drinklink.app.api.InterceptorToken;
import org.drinklink.app.api.V1Initializer;
import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.loader.BaseDataLoader;
import org.drinklink.app.notifications.NotificationsTokenUpdateService;
import org.drinklink.app.service.DrinkLinkFirebaseMessagingNotificationsService;
import org.drinklink.app.utils.GlideLoader;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BaseDataLoader dataLoader);

    void inject(DrinkLinkFragment drinkLinkFragment);

    void inject(ToolbarActivity toolbarActivity);

    void inject(DrinkLinkFirebaseMessagingNotificationsService drinkLinkFirebaseMessagingService);

    void inject(NotificationsTokenUpdateService notificationsTokenUpdateService);

    GlideLoader getGlideLoader();

    Context getContext();

    NotificationsTokenUpdateService getNotificationsTokenUpdateService();

    InterceptorToken getInterceptor();

    V1Initializer initV1Interceptor();
}
