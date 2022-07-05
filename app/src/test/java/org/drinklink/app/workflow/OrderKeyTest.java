/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.workflow;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 *
 */
public class OrderKeyTest {

    @Test
    public void equalsMixerIds() throws Exception {
        ArrayList<Integer> mixerIds1 = new ArrayList<>();
        mixerIds1.add(1);
        ArrayList<Integer> mixerIds2 = new ArrayList<>();
        mixerIds2.add(1);
        assertEquals(new OrderKey(1, mixerIds1, false), new OrderKey(1, mixerIds2, false));
    }

    @Test
    public void notEqualsMixerIds() throws Exception {
        ArrayList<Integer> mixerIds1 = new ArrayList<>();
        mixerIds1.add(1);
        ArrayList<Integer> mixerIds2 = new ArrayList<>();
        mixerIds2.add(2);
        assertNotEquals(new OrderKey(1, mixerIds1, false), new OrderKey(1, mixerIds2, false));
    }

    @Test
    public void notEqualsMixerIdsDifferentCount() throws Exception {
        ArrayList<Integer> mixerIds1 = new ArrayList<>();
        mixerIds1.add(1);
        ArrayList<Integer> mixerIds2 = new ArrayList<>();
        mixerIds2.add(1);
        mixerIds2.add(2);
        assertNotEquals(new OrderKey(1, mixerIds1, false), new OrderKey(1, mixerIds2, false));
    }

}