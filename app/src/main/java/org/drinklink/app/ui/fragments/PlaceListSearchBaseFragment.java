/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.contract.ListAdapterDataSource;
import org.drinklink.app.common.contract.ListAdapterSource;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.common.fragment.CommonListFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.DataLoader;
import org.drinklink.app.model.Place;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.PlaceListItemHolder;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 *
 */

public class PlaceListSearchBaseFragment extends CommonListFragment<List<Place>> {

    private static final String TAG = "PlaceListSearchBaseFragment";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Place place = getProcessor().getPlace();
        if (place != null) {
            place.setUserSelected(false);
            getProcessor().save();
        }
    }

    @NonNull
    @Override
    protected ListAdapterSource getListAdapterSource(final ListAdapterSourceCallback<List<Place>> callback) {

        DataLoader<List<Place>> loader = new DataLoader<List<Place>>(getActivity()) {
            @Override
            protected Observable<List<Place>> getObservable(boolean forceRefresh) {
                ApiService apiService = getApiService();
                return getPlaceObservable(apiService);
            }
        };
        return new ListAdapterDataSource(callback, loader);
    }

    @NonNull
    protected Observable<List<Place>> getPlaceObservable(ApiService apiService) {
        return apiService.getPlaces(0)
                .concatWith(Observable.from(new ArrayList<>()));
//                .flatMapIterable(collection -> collection);
    }

    @Override
    protected ViewModelAdapter getAdapterInstance() {
        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                View.OnClickListener onClick = (view) -> {
                    if (acceptTermsAndConditions(view)) return;
                    showPlace(view);
                };

                add(Place.class, PlaceListItemHolder.getLayout(), view -> new PlaceListItemHolder(view, onClick));
            }
        });
    }

    private void showPlace(View view) {
        Place place = (Place) view.getTag();
        Logger.d(TAG, "Place clicked : " + place.getName());
        getExtras().putString(ExtrasKey.PLACE_EXTRA, getGson().toJson(place));
        getNavigation().next(CategoriesListFragment.class, getExtras(), true);
    }

    private boolean acceptTermsAndConditions(final View view) {
        if (!getPreferencesStorage().isTermsAccepted()) {
            dialog = DialogManager.showAgreeDialog(getActivity(),
                    getString(R.string.terms_and_conditions_title),
                    getText(R.string.terms_and_conditions_message).toString(),
                    getString(R.string.terms_and_condition_agree),
                    () -> {
                        getPreferencesStorage().setTermsAccepted(true);
                        showPlace(view);
                    });
            return true;
        }
        return false;
    }
}
