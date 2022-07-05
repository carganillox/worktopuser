/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@Data
@EqualsAndHashCode()
@AllArgsConstructor
public class WorkHours {

    // monday is 0
    private int dayOfWeek = 0;
    private String startTime;
    private String endTime;
    private boolean isWorkingDay;

    public WorkHours(int dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    //The int value follows the ISO-8601 standard, from 1 (Monday) to 7 (Sunday) ???
}
