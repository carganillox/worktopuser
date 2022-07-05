/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class SignUpCredentials extends Credentials {

    public String email;

    public String passwordConfirmed;

    public SignUpCredentials(String userName, String password, String passwordConfirmed) {
        super(userName, password);
        this.email = userName;
        this.passwordConfirmed = passwordConfirmed;
    }

    public SignUpCredentials(String userName, String password, String email, String passwordConfirmed) {
        super(userName, password);
        this.email = email;
        this.passwordConfirmed = passwordConfirmed;
    }
}
