/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.dependency;

import org.drinklink.app.patch.PatchManager;

import javax.inject.Singleton;

import dagger.Component;

/**
 *
 */
@Singleton
@Component(modules = UpdateModule.class)
public interface UpdateComponent {

    PatchManager getPatchManager();
}
