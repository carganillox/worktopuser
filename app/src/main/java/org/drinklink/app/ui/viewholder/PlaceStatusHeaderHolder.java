/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.model.Place;

import butterknife.BindView;


public class PlaceStatusHeaderHolder extends PlaceHeaderHolder {

    @BindView(R.id.header_avg_preparation)
    TextView tvAvgPreparation;

    public PlaceStatusHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, Place place) {
        super.bind(ctx, position, place);
        if (place != null) {
            setVisibility(tvWorkingHours, false);
            setVisibility(tvAvgPreparation, true);
            tvAvgPreparation.setText(ctx.getString(R.string.place_average_preparation, place.getExpectedOrderPreparationTime()));
        }
    }
}
