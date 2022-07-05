/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenHoursData {

    private String from;
    private String to;
    private Boolean isOpen;

    public Boolean isOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }
}
