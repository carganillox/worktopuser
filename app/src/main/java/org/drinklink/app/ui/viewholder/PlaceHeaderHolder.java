/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.model.Place;
import org.drinklink.app.utils.OpenHoursData;
import org.drinklink.app.utils.TimeUtils;

import butterknife.BindView;
import lombok.Getter;


public class PlaceHeaderHolder extends ViewModelBaseHolder<Place> { //implements IItemUpdated {

    @BindView(R.id.header_place_menu)
    TextView tvHeaderPlace;

    @BindView(R.id.header_place_menu_description)
    TextView tvPlaceDsc;

    @BindView(R.id.header_place_menu_working_hours)
    TextView tvWorkingHours;

    @BindView(R.id.header_is_not_working)
    TextView tvNotWorking;

    @BindView(R.id.header_is_working)
    TextView tvWorking;

    private boolean isWorking;

    public PlaceHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(Context ctx, int position, Place place) {
        super.bind(ctx, position, place);
        if (place != null) {
            tvHeaderPlace.setText(place.getName());
            tvPlaceDsc.setText(place.getAddress());

            OpenHoursData openHours = TimeUtils.getOpenHours(place.getWorkHours(), place.getTimeZoneId());
            tvWorkingHours.setText(openHours.getFrom() != null ? ctx.getString(R.string.place_open_hours, openHours.getFrom(), openHours.getTo()) : "");
            this.isWorking = Boolean.TRUE.equals(openHours.isOpen());
            setVisibility(tvWorking, isWorking);
            setVisibility(tvNotWorking, !isWorking);
        }
    }

    public static int getLayout() {
        return R.layout.include_place_header;
    }
}
