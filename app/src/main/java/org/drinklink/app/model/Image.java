/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import androidx.annotation.NonNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Image extends IdObject {

    private static boolean toggle = false;

    public String url = getPlaceholder();

    @NonNull
    private String getPlaceholder() {
        String imageUrl = toggle ? "http://www.lincoln-park-bars.com/barpics/small-bar-2.jpeg" :
                "https://d1wb0ukcj65cfk.cloudfront.net/restaurant/small-bar-and-kitchen-pda-20140604095428121_lrg.jpg";
        toggle = !toggle;
        return imageUrl;
    }
}
