/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class DrinkListItemBaseHolderTest {

    @Test
    public void testSubstring() {
        String s = "02";
        assertEquals("0", s.substring(0, 1));
        assertEquals("2", s.substring(1, 2));
    }
}