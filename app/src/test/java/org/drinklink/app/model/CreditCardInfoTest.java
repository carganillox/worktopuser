package org.drinklink.app.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class CreditCardInfoTest {

    @Test
    public void cardsEqual() {

        CreditCardInfo c1 = new CreditCardInfo();
        CreditCardInfo c2 = new CreditCardInfo();
        c1.setId(1);
        c2.setId(1);
        assertEquals(c1, c2);
        c2.setId(2);
        assertEquals(c1, c2);
    }
}