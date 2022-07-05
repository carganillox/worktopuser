/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.common.fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.utils.Logger;

/**
 * https://gist.githubusercontent.com/nesquena/d09dc68ff07e845cc622/raw/0d7a99209774f8738d8848229570f3a68823d5ab/EndlessRecyclerViewScrollListener.java
 * https://gist.github.com/nesquena/d09dc68ff07e845cc622
 * https://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 */

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = "EndlessRecyclerViewScrollListener";
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 7;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;

    private final LastVisiblePositionAction lastVisiblePositionAction;


    RecyclerView.LayoutManager layoutManager;

    public EndlessRecyclerViewScrollListener(final LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        lastVisiblePositionAction = new LastVisiblePositionAction() {
            @Override
            public int getLastVisibleItemPosition() {
                return layoutManager.findLastVisibleItemPosition();
            }
        };
    }

    public EndlessRecyclerViewScrollListener(final GridLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
        lastVisiblePositionAction = new LastVisiblePositionAction() {
            @Override
            public int getLastVisibleItemPosition() {
                return layoutManager.findLastVisibleItemPosition();
            }
        };
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {

        if (loading || !checkDirection(dx, dy)) {
            return;
        }
        int totalItemCount = layoutManager.getItemCount();
        if (totalItemCount <= 0) {
            return;
        }
        int lastVisibleItemPosition = lastVisiblePositionAction.getLastVisibleItemPosition();

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if ((lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            Logger.i(TAG, "Load more");
            onLoadMore();
            loading = true;
        }
    }

    protected boolean checkDirection(int dx, int dy) {
        return dx != 0 || dy > 0;
    }

    // Defines the process for actually loading more data based on page
    protected abstract void onLoadMore();

    protected void setLoadFinished() {
        loading = false;
    }

    public boolean isLoading() {
        return loading;
    }

    private interface LastVisiblePositionAction {
        int getLastVisibleItemPosition();
    }
}