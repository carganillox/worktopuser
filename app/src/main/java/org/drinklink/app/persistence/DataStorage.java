/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.drinklink.app.model.Order;
import org.drinklink.app.persistence.model.AnnotationExclusionStrategy;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import rx.Observable;

/**
 *
 */

public class DataStorage {

    private static final String TAG = "DataStore";

    private DbStorage dbStorage;
    private Gson gson;
    private ExecutorService executor;

    @Inject
    public DataStorage(DbStorage dbStorage) {
        this.dbStorage = dbStorage;
        this.gson = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy()).create();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Observable<List<OrderPreparation>> getOrderPreparations() {
        return Observable.fromCallable(this::getItems);
    }

    // TODO user ORM
    private List<OrderPreparation> getItems() {
        return getItems(null);
    }

    // TODO user ORM
    private List<OrderPreparation> getItems(Integer orderId) {
        Cursor cursor = dbStorage.readCursorRecent(orderId);
        List<OrderPreparation> items= new ArrayList<>();
        while(cursor.moveToNext()) {
            String dataJson = cursor.getString(cursor.getColumnIndexOrThrow(DbColumns.OrderEntry.COLUMN_NAME_DATA));
            try {
                OrderPreparation orderPreparation = gson.fromJson(dataJson, OrderPreparation.class).updateSelection();
                items.add(orderPreparation);
            } catch (JsonSyntaxException ex) {
                FirebaseCrashlytics.getInstance().recordException(ex);
                FirebaseCrashlytics.getInstance().log(ex.getMessage() + ", json: " + dataJson);
            }
        }
        cursor.close();
        return items;
    }

    public void addOrUpdateOrderPreparation(final OrderPreparation orderPreparation, final List<OrderPreparation> pendingOrder) {

        orderPreparation.updateList();
        captureLastModified(orderPreparation.getOrder());

        executor.execute(() -> {
            Long removeId = null;
            List<ContentValues> valuesList = new ArrayList<>();
            // add/update not published or remove if empty
            if (orderPreparation.getOrder() != null) {
                valuesList.add(toContentValues(orderPreparation));
            }

            for (OrderPreparation order : pendingOrder) {
                if (order.getOrder().isUpdated()) {
                    valuesList.add(toContentValues(order));
                }
            }
            dbStorage.addOrReplace(DbColumns.OrderEntry.TABLE_NAME, valuesList, removeId);
        });
    }

    @NonNull
    private ContentValues toContentValues(OrderPreparation orderPreparation) {

        String dataJsonString = gson.toJson(orderPreparation);
        Order order = orderPreparation.getOrder();
        Logger.i(TAG, "save " + " " + orderPreparation.getId() + " "  + dataJsonString);
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DbColumns.OrderEntry.COLUMN_NAME_DATA, dataJsonString);
        values.put(DbColumns.OrderEntry._ID, orderPreparation.getId());
        values.put(DbColumns.OrderEntry.COLUMN_NAME_IS_FINISHED, order != null ? (order.isFinished() ? 1 : 0) : 0);
        values.put(DbColumns.OrderEntry.COLUMN_NAME_LAST_MODIFIED, order != null ? order.getLastModified() : Long.MAX_VALUE);
        return values;
    }

    private void captureLastModified(Order order) {
        if (order != null) {
            order.captureLastModified();
        }
    }

    public OrderPreparation getOrderPreparation(int orderId) {
        List<OrderPreparation> items = getItems(orderId);
        return items.isEmpty() ? null: items.get(0);
    }

    public void deleteOrderPreparation(int id) {
        dbStorage.delete(DbColumns.OrderEntry.TABLE_NAME, id);
    }

    public void clear() {
        dbStorage.deleteAll(DbColumns.OrderEntry.TABLE_NAME);
    }
}
