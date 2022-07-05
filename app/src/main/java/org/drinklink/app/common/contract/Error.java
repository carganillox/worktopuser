/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.contract;

/**
 *
 */

public class Error {

    private String message;

    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
