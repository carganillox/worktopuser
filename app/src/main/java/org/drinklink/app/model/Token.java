/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 */
@Data
@AllArgsConstructor
public class Token {

    public Token(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    private String token;

    private String refreshToken;

    private Date expiration;
}
