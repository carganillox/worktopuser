package org.drinklink.app.common.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinklink.app.R;
import org.drinklink.app.common.viewholder.ViewModelBaseHolder;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.common.viewmodel.ViewModelItem;
import org.drinklink.app.common.viewmodel.ViewModelListItem;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class ViewModelAdapter<T> extends RecyclerView.Adapter<ViewModelBaseHolder> {

    public static final String TAG = "ViewModelAdapter";

    public List<T> dataItems = new ArrayList<>();
    protected Context mContext;
    protected ViewModelHolderFactory factory;
    protected boolean showProgressBar = false;
    protected ViewModelItem progressBarViewModel =
            new ViewModelItem(R.layout.include_main_progress_bar);
    private boolean clearOnNextElement = true;

    public ViewModelAdapter(Context ctx, ViewModelHolderFactory factory) {
        this(ctx, new ArrayList<T>(), factory);
    }

    public ViewModelAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public ViewModelAdapter(Context ctx, List<T> items, ViewModelHolderFactory factory) {
        dataItems.addAll(items);
        this.mContext = ctx;
        this.factory = factory;
    }

    public void replaceItems(List<T> items) {
        dataItems.clear();
        dataItems.addAll(items);
        notifyDataSetChanged();
    }

    public void clearData() {
        showProgressBar = false;
        dataItems.clear();
        notifyDataSetChanged();
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = null;
        try {
            boolean isProgressBar = position >= getSize();
            item = isProgressBar ? progressBarViewModel : getPosition(position);
            return factory.getType(item);
        } catch (NullPointerException npe) {
            Logger.e(TAG, "getItemViewType NPE, will show progress bar, " +
                    item != null  ? item.getClass().toString() : "nothing on position", npe);
            throw npe;
        } catch (IndexOutOfBoundsException ex) {
            Logger.e(TAG, "getItemViewType failed, will show progress bar, " +
                    item != null  ? item.getClass().toString() : "nothing on position", ex);
            return factory.getType(progressBarViewModel);
        }
    }

    @Override
    public ViewModelBaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(factory.getLayout(viewType), parent, false);
        return factory.getHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewModelBaseHolder holder, int position) {
        try {
            if (getSize() > position) {
                Object currentItem = getPosition(position);
                Object previousItem = position > 0 ? getPosition(position - 1) : null;
                holder.bind(getContext(), position, getBindItem(currentItem));

                // Think differently! Got to be better solution for this!
                if (previousItem == null) {
                    holder.comparePredecessorItem(currentItem, null);
                } else if (currentItem.getClass().equals(previousItem.getClass())) {
                    holder.comparePredecessorItem(currentItem, previousItem);
                }
            } // progress bar
        } catch (IndexOutOfBoundsException ex) {
            Logger.e(TAG, "items changed", ex);
        }
    }

    private Object getBindItem(Object currentItem) {
        if (currentItem instanceof ViewModelListItem) {
            return ((ViewModelListItem) currentItem).getData();
        }
        return currentItem;
    }

    @Override
    public void onViewRecycled(ViewModelBaseHolder holder) {
        holder.onViewRecycled();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        int size = getSize();
        return size + (size > 0 && showProgressBar ? 1 : 0);
    }

    protected int getSize() {
        return getDataCount();
    }

    public int getDataCount() {
        return dataItems.size();
    }

    protected Object getPosition(int position) {
        return dataItems.get(position);
    }



    public void appendItems(List<T> items) {
        clearOnNextElementIfNeeded();
        showProgressBar = false;
        dataItems.addAll(items);
        notifyDataSetChanged();
    }

    public void appendItem(T item) {
        clearOnNextElementIfNeeded();
        dataItems.add(item);
        notifyItemInserted(dataItems.size() - 1);
    }

    private void clearOnNextElementIfNeeded() {
        if (clearOnNextElement) {
            dataItems.clear();
            clearOnNextElement = false;
        }
    }

    public void removeItem(T item) {
        notifySingleItemRemove(item);
        dataItems.remove(item);
    }

    public void clearOnNextElement() {
        this.clearOnNextElement = true;
    }

    public void showItems() {
        notifyDataSetChanged();
    }

    public void appendTop(T item) {
        dataItems.add(0, item);
        notifyItemInserted(0);
    }

    public void appendTop(List<T> items) {
        dataItems.addAll(0, items);
        notifyDataSetChanged();
    }

    public void insert(T item, int position) {
        try{
            if (position >= 0 && position <=  getDataCount()) { // do not use getSize, because position do not takes into account header
                dataItems.add(position, item);
                notifyItemInserted(position);
            }
        } catch (IndexOutOfBoundsException ex) {
            Logger.e(TAG, "lost changed while inserting", ex);
        }
    }

    public void showProgressBar() {
        setProgressBar(true);
    }

    public void hideProgressBar() {
        setProgressBar(false);
    }

    public boolean isShowingProgressBar() {
        return showProgressBar;
    }

    private void setProgressBar(boolean show) {
        showProgressBar = show;
        try {
            notifyDataSetChanged();
        } catch (IllegalStateException ex) {
            Logger.e(TAG, "prevent crash when adding progress bar while scrolling", ex);
        }
    }

    public void setFactory(ViewModelHolderFactory factory) {
        this.factory = factory;
    }

    public int insertBehind(Object positionElement, T itemToInsert) {
        int index = dataItems.indexOf(positionElement);
        int position = index + 1;
        if (index != -1) {
            insert(itemToInsert, position);
        }
        return position;
    }

    public int insertBehind(ListUtil.Condition<T> condition, T itemToInsert) {
        Object first = ListUtil.findFirst(dataItems, condition);
        return insertBehind(first, itemToInsert);
    }

    public void notifySingleItemChanged(Object orig) {
        int position = dataItems.indexOf(orig);
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public void notifySingleItemInserted(Object orig) {
        int position = dataItems.indexOf(orig);
        if (position >= 0) {
            notifyItemInserted(position);
        }
    }

    // this should be used only together with dataItems remove
    private void notifySingleItemRemove(Object orig) {
        int position = dataItems.indexOf(orig);
        if (position >= 0) {
            notifyItemRemoved(position);
        }
    }

    public List<T> getDataItems() {
        return dataItems;
    }

    public boolean hasItems() {
        return !dataItems.isEmpty();
    }

    public void rebindAll() {
        for (Object item: dataItems) {
            notifySingleItemChanged(item);
        }
    }
}
