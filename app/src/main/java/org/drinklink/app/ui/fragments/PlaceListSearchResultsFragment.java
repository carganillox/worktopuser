/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.model.Place;

import java.util.List;

import butterknife.BindView;
import rx.Observable;

/**
 *
 */

public class PlaceListSearchResultsFragment extends PlaceListSearchBaseFragment {

    @BindView(R.id.header_search)
    TextView header;

    @BindView(R.id.header_search_description)
    TextView headerDescription;

    private String keyword;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_search_result_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
        headerDescription.setText("");
    }

    @Override
    protected void finishLoading(boolean completed) {
        super.finishLoading(completed);
        if (completed) {
            int dataCount = adapter.getDataCount();
            String count = Integer.toString(dataCount);
            String string;
            switch (dataCount) {
                case 0:
                    string = getString(R.string.search_results_0);
                    break;
                case 1:
                    string = getString(R.string.search_results_1);
                    break;
                default:
                    string = getString(R.string.search_results_more, count);
            }
            headerDescription.setText(string);
        }
    }

    private void initFragment() {
        if (header != null) {
            header.setText(keyword);
        }
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        keyword = bundle.getString(ExtrasKey.SEARCH_KEYWORD_EXTRA);
        initFragment();
    }

    @NonNull
    @Override
    protected Observable<List<Place>> getPlaceObservable(ApiService apiService) {
        Observable<Place> placeObservable = super.getPlaceObservable(apiService)
                .flatMapIterable(items -> items);
        Observable<Place> filter = placeObservable
                .filter(place -> PlaceListSearchResultsFragment.this.isMatch(place.getName(), keyword) ||
                        PlaceListSearchResultsFragment.this.isMatch(place.getAddress(), keyword) ||
                        PlaceListSearchResultsFragment.this.isMatch(place.getDescription(), keyword));
        return filter.toList();
    }

    private boolean isMatch(String s, String keyword) {
        return s != null && normalize(s).contains(normalize(keyword));
    }

    private String normalize(String keyword) {
        return keyword.toLowerCase().replaceAll("\\s+","").replace(".", "").replace(",", "");
    }
}
