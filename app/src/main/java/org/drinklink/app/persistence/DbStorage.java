/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.drinklink.app.persistence.DbColumns.OrderEntry;

/**
 *
 */
@Singleton
public class DbStorage {

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    @Inject
    public DbStorage(Context context) {
        dbHelper = new DbHelper(context);
        // Gets the data repository in write mode
        db = dbHelper.getWritableDatabase();
    }


    public Cursor readCursorRecent(Integer orderId) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                OrderEntry.COLUMN_NAME_DATA
        };

        String selection;
        String[] selectionArgs;
        if (orderId == null) {
            // Filter results WHERE "title" = 'My Title'
            selection = OrderEntry.COLUMN_NAME_LAST_MODIFIED + " > ?";
            // last 2 days
            String millisTwoDaysAgo = Long.toString(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS));
            selectionArgs = new String[] {millisTwoDaysAgo};
            // TODO: or order is still open
        } else{
            // Filter results WHERE "title" = 'My Title'
            selection = OrderEntry._ID + " = ?";
            // last 2 days
            selectionArgs = new String[] {Integer.toString(orderId)};
        }

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                OrderEntry.COLUMN_NAME_IS_FINISHED + " ASC, " +
                OrderEntry.COLUMN_NAME_LAST_MODIFIED + " DESC";

        Cursor cursor = db.query(
                OrderEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        return cursor;
    }



    public long addOrReplace(String tableName, ContentValues values) {

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return newRowId;
    }

    public List<Long> addOrReplace(String tableName, List<ContentValues> valuesList, Long removeNotPublished) {
        List<Long> ids = new ArrayList<>();
        db.beginTransaction();
        try {
            if (removeNotPublished != null) {
                delete(tableName, removeNotPublished);
            }
            for (ContentValues values : valuesList) {
                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                ids.add(newRowId);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return ids;
    }

    void update() {
        // New value for one column
//        String title = "MyNewTitle";
//        ContentValues values = new ContentValues();
//        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
//
//// Which row to update, based on the title
//        String selection = FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
//        String[] selectionArgs = { "MyOldTitle" };
//
//        int count = db.update(
//                FeedReaderDbHelper.FeedEntry.TABLE_NAME,
//                values,
//                selection,
//                selectionArgs);

    }

    int delete(String tableName, long id) {
        // Define 'where' part of query.
        String selection = DbColumns.OrderEntry._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { Long.toString(id) };
        // Issue SQL statement.
        int deletedRows = db.delete(tableName, selection, selectionArgs);
        return deletedRows;
    }


    public void deleteAll(String tableName) {
        int deletedRows = db.delete(tableName, null, null);
    }
}
