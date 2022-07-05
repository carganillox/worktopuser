/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import android.provider.BaseColumns;

/**
 *
 */

public class DbColumns {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DbColumns() {}

    /* Inner class that defines the table contents */
    public static class OrderEntry implements BaseColumns {
        public static final String TABLE_NAME = "OrderRequest";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_IS_FINISHED = "is_finished";
        public static final String COLUMN_NAME_LAST_MODIFIED = "last_modified";
    }

}
