package org.drinklink.app.persistence;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class AuthTokenTest {

    @Test
    public void getEncoded() {
        AuthToken authToken = new AuthToken();
        authToken.setUsername("test+51@gmail.com");
        String urlUsername = authToken.getUrlUsername();
        assertEquals("test%2B51%40gmail.com", urlUsername);
    }
}