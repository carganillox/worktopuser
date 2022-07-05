/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import android.util.Log;

import org.drinklink.app.model.WorkHours;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class TimeUtilsTest {


    private static final String MIDNIGHT = "23:59:59";
    private static final String MIDNIGHT_2 = "23:59:59.9990000";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void getOpenHours1Item() throws Exception {
        for (int i = 0; i < 7; i++) {
            getOpenHours(i);
        }
    }

    private void getOpenHours(int day) {
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(day, "20:00:00.000", "23:00:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, day, (20 * 3600) * 1000);
        assertEquals("8pm - 11pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (19 * 3600) * 1000);
        assertEquals("8pm - 11pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (21 * 3600) * 1000);
        assertEquals("8pm - 11pm", getOpenHoursString(openHours));
    }

    @Test
    public void getOpenHoursHalfHour() {
        int day = 0;
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(day, "20:30:00.000", "23:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, day, (20 * 3600) * 1000);
        assertEquals("8:30pm - 11:30pm", getOpenHoursString(openHours));
    }

    @Test
    public void getOpenHoursTwoSameDay() {
        int day = 0;
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(day, "15:30:00.000", "18:30:00.000"));
        list.add(new WorkHours(day, "20:30:00.000", "23:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, day, (14 * 3600) * 1000);
        assertEquals("3:30pm - 6:30pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (16 * 3600) * 1000);
        assertEquals("3:30pm - 6:30pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (19 * 3600) * 1000);
        assertEquals("8:30pm - 11:30pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (21 * 3600) * 1000);
        assertEquals("8:30pm - 11:30pm", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, day, (23 * 3600 + 1802) * 1000);
        assertEquals("8:30pm - 11:30pm", getOpenHoursString(openHours));
    }


    @Test
    public void getOpenHoursConsecutiveDaysNotContinued() {
        int day = 0;
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(day, "15:30:00.000", "18:30:00.000"));
        list.add(new WorkHours(day + 1, "20:30:00.000", "23:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, day, (14 * 3600) * 1000);
        assertEquals("3:30pm - 6:30pm", getOpenHoursString(openHours));
    }

    @Test
    public void getOpenHoursConsecutiveDaysContinued() {
        int day = 0;
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(day, "15:30:00.000", MIDNIGHT));
        list.add(new WorkHours(day + 1, "00:00:00.000", "04:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, day, (14 * 3600) * 1000);
        assertEquals("3:30pm - 4:30am", getOpenHoursString(openHours));
    }

    @Test
    public void getOpenHoursLastDay() {

        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(0, "15:30:00.000", MIDNIGHT));
        list.add(new WorkHours(1, "15:30:00.000", MIDNIGHT));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, 1, (14 * 3600) * 1000);
        assertEquals("3:30pm - 12am", getOpenHoursString(openHours));
    }


    @Test
    public void getOpenHoursConsecutiveDaysContinuedSundayMonday() {
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(6, "15:30:00.000", MIDNIGHT));
        list.add(new WorkHours(7, "00:00:00.000", "05:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, 6, (14 * 3600) * 1000);
        assertEquals("3:30pm - 5:30am", getOpenHoursString(openHours));

        openHours = TimeUtils.getOpenHours(list, 6, (20 * 3600) * 1000);
        assertEquals("3:30pm - 5:30am", getOpenHoursString(openHours));
    }

    @Test
    public void mergeWithPreviousDay() {
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(6, "15:30:00.000", MIDNIGHT));
        list.add(new WorkHours(7, "00:00:00.000", "05:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, 7, (1 * 3600) * 1000);
        assertEquals("3:30pm - 5:30am", getOpenHoursString(openHours));
    }

    @Test
    public void mergeWithPreviousDay2() {
        ArrayList<WorkHours> list = new ArrayList<>();
        list.add(new WorkHours(6, "15:30:00.000", MIDNIGHT_2));
        list.add(new WorkHours(7, "00:00:00.000", "05:30:00.000"));

        OpenHoursData openHours = TimeUtils.getOpenHours(list, 7, (1 * 3600) * 1000);
        assertEquals("3:30pm - 5:30am", getOpenHoursString(openHours));
    }

    @Test
    public void getMillisecondsOfTime() throws Exception {
        long millisecondsOfTime = TimeUtils.getTimeInMillis("00:00:00.001");
        assertEquals(0, millisecondsOfTime);

        millisecondsOfTime = TimeUtils.getTimeInMillis("00:00:00.101");
        assertEquals(0, millisecondsOfTime);

        millisecondsOfTime = TimeUtils.getTimeInMillis("00:00:02.001");
        assertEquals(2000, millisecondsOfTime);

        millisecondsOfTime = TimeUtils.getTimeInMillis("00:01:02.001");
        assertEquals(62000, millisecondsOfTime);
    }

    private static String getOpenHoursString(OpenHoursData openHours) {
        return openHours.getFrom() + " - " + openHours.getTo();
    }

    @Test
    public void testDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int day = (currentDay + 5) % 7;
    }

    @Test
    public void collectAt() {
        long collect = TimeUtils.getDateTimeInMillisFromString("2019-09-23T20:20:11.8539151Z");
        long current = TimeUtils.getCurrentTimeMs();
        long diff = (collect - current)/1000;

//        assertTrue(diff > 0);
    }


}