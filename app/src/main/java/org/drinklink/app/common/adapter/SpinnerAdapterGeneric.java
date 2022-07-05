/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 *
 */

public abstract class SpinnerAdapterGeneric<T> extends ArrayAdapter<T> {

    private final int textViewResourceId;
    private Context context;
    private T[] values;

    public SpinnerAdapterGeneric(Context context, int textViewResourceId,
                                 T[] values) {
        super(context, textViewResourceId, values);
        this.textViewResourceId = textViewResourceId;
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public T getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(textViewResourceId, parent, false);
        }
        TextView label = (TextView) convertView;
        T value = values[position];
        label.setText(getItemText(value));
        return label;
    }

    protected abstract String getItemText(T value);

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}