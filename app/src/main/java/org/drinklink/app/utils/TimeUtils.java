/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinklink.app.model.WorkHours;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 *
 */

public class TimeUtils {

    private static final String TAG = "TimeUtils";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = getSimpleDateFormat();

    public static final long MAX_MILLIS_TIME = 9999999999999L;

    @NonNull
    private static SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
        return simpleDateFormat;
    }

    public static final long SECOND_IN_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long MINUTE_IN_SEC = TimeUnit.MINUTES.toSeconds(1);

    private static final DateFormat FORMATTER_MS = getFormatter("HH:mm:ss.SSSSSSS");
    private static final DateFormat FORMATTER = getFormatter("HH:mm:ss");
    private static final DateFormat DATE_TIME_FORMATTER = getFormatter("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateFormat FORMATTER_UI = getFormatter("h:mma");
    private static final DateFormat FORMATTER_UI_HOURS = getFormatter("ha");
    private static final long CONTINUES_END_TIME = getTimeInMillis("23:59:59");//("23:59:59.999");
    private static final long SECOND_MS = 1000;
    private static final long MINUTE_MS = 60 * SECOND_MS;
    private static final long HOUR_MS = 60 * MINUTE_MS;

    @NonNull
    private static SimpleDateFormat getFormatter(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }

    private TimeUtils() {
    }

    public static String getTimeFormat(long milliseconds) {
        long seconds = milliseconds / SECOND_IN_MS;
        seconds = seconds >= 0 ? seconds : 0;
        return String.format("%1$02d:%2$02d", seconds / MINUTE_IN_SEC, seconds % MINUTE_IN_SEC);
    }

    public static String getTimeFormatForTime(long milliseconds) {
        return SIMPLE_DATE_FORMAT.format(new Date(milliseconds));
    }

    @NonNull
    public static OpenHoursData getOpenHours(List<WorkHours> workHours, String timeZoneId) {
        TimeZone timeZone = timeZoneId != null && !"".equals(timeZoneId) ? TimeZone.getTimeZone(timeZoneId) : null;
        Calendar midnightCalender = getMidnightCalenderForTimeZone(timeZone);
        int day = (midnightCalender.get(Calendar.DAY_OF_WEEK) - 1);
        long timeInMillis = getCalenderForTimeZone(timeZone).getTimeInMillis() - midnightCalender.getTimeInMillis();
        return getOpenHours(workHours, day, timeInMillis);
    }

//    private static long getTimeInMillis(long timeInMillis, Calendar cal) {
//        long midnight = cal.getTimeInMillis();
//        return timeInMillis - midnight;
//    }

    @NonNull
    private static Calendar getMidnightCalenderForTimeZone(TimeZone timeZone) {
        Calendar cal = getCalenderForTimeZone(timeZone);
        // set to mid-night
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private static Calendar getCalenderForTimeZone(TimeZone timeZone) {
        return timeZone != null ? Calendar.getInstance(timeZone) : Calendar.getInstance();
    }

    @NonNull
    public static OpenHoursData getOpenHours(List<WorkHours> workHours, int day, long timeInMillis) {
        OpenHoursData openHours = new OpenHoursData();
        if (workHours == null) {
            workHours = new ArrayList<>();
        }

        workHours = sortAndFilterWorkhours(workHours);

        for (int i = 0; i < workHours.size(); i++) {
            WorkHours wh = workHours.get(i);
            long endTimeMillis = getTimeInMillis(wh.getEndTime());
            if (day == wh.getDayOfWeek()) {
                boolean isShiftOver = timeInMillis > endTimeMillis;
                boolean hasAnotherShiftSameDay = workHours.size() > i + 1 &&
                                                 workHours.get(i + 1).getDayOfWeek() == day;
                if (!isShiftOver || !hasAnotherShiftSameDay) {
                    asOpenHours(workHours, day, i, openHours, timeInMillis);
                    break;
                } // else move to next Shift in the same day
            }
        }
        return openHours;
    }

    private static List<WorkHours> sortAndFilterWorkhours(List<WorkHours> workHours) {
        Collections.sort(workHours, (lhs, rhs) -> {
            // -1 - less than, 1 - greater than, 0 - equal, all inverted for descending
            return lhs.getDayOfWeek() > rhs.getDayOfWeek() ? 1 :
                    (lhs.getDayOfWeek() < rhs.getDayOfWeek()) ? -1 : compareTime(lhs, rhs);
        });
        return ListUtil.select(workHours, item -> item.isWorkingDay());
    }

    @NonNull
    private static OpenHoursData asOpenHours(List<WorkHours> workHours, int day, int i,
                                             OpenHoursData openHours, long timeInMillis) {
        WorkHours wh = workHours.get(i);
        long endTimeMillis = getTimeInMillis(wh.getEndTime());
        Date startTime = getDate(wh.getStartTime());
        openHours.setFrom(toUiTime(startTime));
        boolean isTimeAfterOpening = timeInMillis >= startTime.getTime();

        // shift ends before midnight, don't connect to prev day shift
        if (endTimeMillis < CONTINUES_END_TIME) {
            WorkHours prevWh = workHours.get(i > 0 ? i - 1 : workHours.size() - 1);
            // this shift starts at midn. and prev ends at midn. connect with previous
            if (getTimeInMillis(wh.getStartTime()) == 0 &&
                    getTimeInMillis(prevWh.getEndTime()) == CONTINUES_END_TIME) {
                openHours.setFrom(toUiTime(getDate(prevWh.getStartTime())));
            }
            // end time is this day
            Date endTime = getDate(wh.getEndTime());
            openHours.setTo(toUiTime(endTime));
            openHours.setOpen(isTimeAfterOpening && endTime.getTime() >= timeInMillis);

        } else { // shift ends at midnight connect to next day shift if needed
            //end time is 1) next day, or 2) next day sunday, or 3) before midnight
            openHours.setOpen(isTimeAfterOpening);
            if (workHours.size() > i + 1 &&
                    workHours.get(i + 1).getDayOfWeek() == day + 1 &&
                    getTimeInMillis(workHours.get(i + 1).getStartTime()) == 0) {
                // end time is next day
                 openHours.setTo(toUiTime(workHours.get(i + 1).getEndTime()));
            } else if (i > 0 && i == workHours.size() - 1 &&
                    workHours.get(0).getDayOfWeek() + 7 == (day + 1) &&
                    getTimeInMillis(workHours.get(0).getStartTime()) == 0) {
                // end time is next day, sunday
                openHours.setTo(toUiTime(workHours.get(0).getEndTime()));
            } else {
                // end time is this day Midnight
                openHours.setTo(toUiTime(wh.getEndTime()));
            }
        }
        return openHours;
    }

    private static String toUiTime(String timeString) {
        Date date = getDate(timeString);
        return toUiTime(date);
    }

    private static String toUiTime(Date date) {
        return (date.getTime() % 3600000 > 0 ? FORMATTER_UI.format(date) : FORMATTER_UI_HOURS.format(date))
                .replace("AM", "am")
                .replace("PM","pm");
    }

    private static int compareTime(WorkHours lhs, WorkHours rhs) {
        long lTime = getTimeInMillis(lhs.getStartTime());
        long rTime = getTimeInMillis(rhs.getStartTime());
        return lTime > rTime ? 1 : lTime < rTime ? -1 : 0;
    }

    public static long getTimeInMillis(String timeString) {
        Date dt = getDate(timeString);
        return dt != null ? dt.getTime() : 0;
    }

    public static long getDateTimeInMillisFromString(String timeString) {
        Date dt = getDate(timeString, DATE_TIME_FORMATTER);
        return dt != null ? dt.getTime() : 0;
    }

    private static String fixMillis(String timeString) {
//        return timeString.replace("9990000", "999");
        return timeString.split("\\.", 2)[0];
    }

    @Nullable
    private static Date getDate(String timeString) {
        Date dt = getDateFromString(timeString);
        long plusMinute = dt.getTime() + MINUTE_MS;
        plusMinute = plusMinute - plusMinute % MINUTE_MS;
        long modeHours = plusMinute % HOUR_MS;
        if (modeHours == 0) {
            dt = new Date(plusMinute);
        }
        return dt;
    }

    private static Date getDateFromString(String timeString) {
        return getDate(timeString, FORMATTER);
    }

    private static Date getDate(String timeString, DateFormat formatter) {
        if (timeString == null) {
            return new Date(0);
        }
        String timeStringFixed = fixMillis(timeString);
        Date dt;
//        try {
//            dt = FORMATTER_MS.parse(timeStringFixed);
//        } catch (ParseException e) {
        try {
            dt = formatter.parse(timeStringFixed);
        } catch (ParseException ex) {
            Logger.e(TAG, ex.getMessage(), ex);
            return new Date();
        }
//        }
//        if (dt.getTime() % 1000 == 999) { // increase .999 for 1 ms
//            dt.setTime(dt.getTime() + 1);
//        } // 23.59.59 => 00:00:00
        return dt;
    }

    public static long getCurrentTimeMs() {
        return System.currentTimeMillis();
    }
}
