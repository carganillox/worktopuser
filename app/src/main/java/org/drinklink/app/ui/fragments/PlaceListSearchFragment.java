/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.drinklink.app.R;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.utils.Logger;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */

public class PlaceListSearchFragment extends PlaceListSearchBaseFragment {

    private static final String TAG = "PlaceListSearchFragment";

    @BindView(R.id.edit_text_keyword)
    EditText etKeyword;

    @BindView(R.id.button_search)
    Button btnSearch;

    @BindView(R.id.text_view_keyword)
    TextView tvKeyword;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_search_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getAnalytics().appOpen();
        super.onViewCreated(view, savedInstanceState);
        bindEditTextWithLabelVisibility(etKeyword, tvKeyword);
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean searchEnabled = isSearchEnabled(editable);
                Logger.d(TAG, "search enabled: " + searchEnabled);
                btnSearch.setEnabled(searchEnabled);
            }
        });
    }

    private boolean isSearchEnabled(Editable editable) {
        String keyword = editable.toString();
        boolean empty = TextUtils.isEmpty(keyword) || keyword.length() <= 2;
        return !empty;
    }

    @OnClick(R.id.button_search)
    void onSearch() {
        String keyword = etKeyword.getText().toString();
        Logger.d(TAG, "search :" + keyword);
        if (!isSearchEnabled(etKeyword.getEditableText())) {
            return;
        }
        hideKeyboard();
        getExtras().putString(ExtrasKey.SEARCH_KEYWORD_EXTRA, keyword);
        getNavigation().next(PlaceListSearchResultsFragment.class, getExtras(), true);
    }

    private void hideKeyboard() {
        etKeyword.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }
}
