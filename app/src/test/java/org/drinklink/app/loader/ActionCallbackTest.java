package org.drinklink.app.loader;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ActionCallbackTest {

    @Ignore
    @Test
    public void fixErrorBody() {
        String test = "\"The LastName field is required.\r\n\"";
        assertEquals("The Last Name field is required.", ActionCallback.fixErrorBody(test));
    }

    @Ignore
    @Test
    public void fixErrorBodyInTheMiddle() {
        String test = "\"An error occured while creating a user. Error: Could not register user. PasswordTooShort: Passwords must be at least 8 characters.\r\nPasswordRequiresUpper: Passwords must have at least one uppercase ('A'-'Z').\r\n\"";
        String actual = ActionCallback.fixErrorBody(test);
        assertEquals("An error occured while creating a user. Error: Could not register user. Password Too Short: Passwords must be at least 8 characters. Password Requires Upper: Passwords must have at least one uppercase ('A'-'Z').", actual);
    }
}