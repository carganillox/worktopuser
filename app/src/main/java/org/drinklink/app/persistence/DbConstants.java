/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import static org.drinklink.app.persistence.DbColumns.OrderEntry;

/**
 *
 */

public class DbConstants {

    private DbConstants() {
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + OrderEntry.TABLE_NAME + " (" +
                    OrderEntry._ID + " INTEGER PRIMARY KEY," +
                    OrderEntry.COLUMN_NAME_IS_FINISHED + " INTEGER," +
                    OrderEntry.COLUMN_NAME_LAST_MODIFIED + " INTEGER," +
                    OrderEntry.COLUMN_NAME_DATA + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + OrderEntry.TABLE_NAME;



}
