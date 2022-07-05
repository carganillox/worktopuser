package org.drinklink.app.common.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.contract.Error;
import org.drinklink.app.common.contract.ListAdapterSource;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.utils.Logger;

import java.util.List;

import butterknife.BindView;

public abstract class CommonListFragment<TViewModel> extends DrinkLinkFragment {

    private static final String TAG = "CommonListFragment";

    @NonNull
    protected abstract ListAdapterSource getListAdapterSource(ListAdapterSourceCallback<TViewModel> callback);

    protected abstract ViewModelAdapter getAdapterInstance();

    @BindView(R.id.main_list) RecyclerView list;

    protected LinearLayoutManager layoutManager;
    protected ViewModelAdapter adapter;
    protected ListAdapterSource source;
    protected  EndlessRecyclerViewScrollListener scrollListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        DependencyResolver.getComponent().inject(this);
        layoutManager = instantiateLinearLayoutManager();
        list.setLayoutManager(layoutManager);
        addScrollListener();
        showProgressBar(true);
        processSavedBundleInstance(savedInstanceState);
        return rootView;
    }

    private void addScrollListener() {
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore() {
                source.loadMoreData();
            }
        };
        list.addOnScrollListener(scrollListener);
    }

    protected void processSavedBundleInstance(Bundle savedInstanceState) {
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_list;
    }

    @NonNull
    protected LinearLayoutManager instantiateLinearLayoutManager() {
        return new LinearLayoutManager(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG, "onViewCreated");
        adapter = getAdapterInstance();
        source = getListAdapterSource(new ListAdapterSourceCallback<TViewModel>() {

            @Override
            public void onNext(TViewModel data, String callToken) {
                Logger.i(TAG, "loader.onNext");
                finishLoading(false);
                if (data instanceof List) {
                    adapter.appendItems((List)data);
                } else {
                    adapter.appendItem(data);
                }
            }

            @Override
            public void onCompleted() {
                Logger.i(TAG, "loader.onCompleted");
                finishLoading(true);
                list.removeOnScrollListener(scrollListener);
            }

            @Override
            public void onError(Error error, String callToken) {
                Logger.e(TAG, "loader.onError" + error.getMessage());
                showToast(error.getMessage());
                finishLoading(true);
            }
        });

        list.setAdapter(adapter);
    }

    protected void finishLoading(boolean completed) {
        if (scrollListener.isLoading()) {
            scrollListener.setLoadFinished();
            Logger.i(TAG, "scroll.finished");
        }
        if (completed) {
            adapter.hideProgressBar();
            Logger.i(TAG, "listView.hideProgressBar");
        }
        if (!adapter.hasItems()) { // on first loaded hide progress bar
            Logger.i(TAG, "listView.firstItem");
            showProgressBar(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        source.unSubscribe();
        super.onPause();
    }

    protected void refresh() {
        Logger.d(TAG, "onRefresh");
        adapter.clearOnNextElement();
        adapter.showProgressBar();
        source.onRefresh(false);
    }
}
