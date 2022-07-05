/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import android.text.TextUtils;
import android.view.View;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ActivityResults;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.contract.ListAdapterDataSource;
import org.drinklink.app.common.contract.ListAdapterSource;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.common.fragment.CommonListFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.DataLoader;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.persistence.DbColumns;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.activities.OrderPreparationPreviewActivity;
import org.drinklink.app.ui.activities.OrderStatusActivity;
import org.drinklink.app.ui.viewholder.OrderPreparationItemHolder;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import lombok.Data;
import lombok.EqualsAndHashCode;
import rx.Observable;

/**
 *
 */

public class OrderHistoryFragment extends CommonListFragment<OrderPreparation> implements IOrderProcessor.OrderUpdateListener {

    private static final String TAG = "OrderHistoryFragment";

    private List<OrderingOption> orderingOption;

    @BindView(R.id.spinner_history_ordering)
    AppCompatButton spinnerOrdering;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initOptions();
        bindSpinnersDrawableRight(spinnerOrdering);
    }

    @Override
    public void onResume() {
        super.onResume();
        getProcessor().addOrderUpdateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getProcessor().removeOrderUpdateListener(this);
    }

    @Override
    public boolean onOrderUpdated(Order order, OrderStates previousState) {
        List dataItems = adapter.getDataItems();
        OrderPreparation matchingPreparation = (OrderPreparation)ListUtil.findFirst(dataItems,
                item -> ((OrderPreparation) item).getId() == order.getId());
        if (matchingPreparation == null) {
            return false;
        }
        matchingPreparation.getOrder().setCurrentState(order.getCurrentState());
        adapter.notifySingleItemChanged(matchingPreparation);
        return false;
    }

    @Override
    public boolean isMatch(int orderId) {
        return true;
    }

    @Override
    public boolean onOrderAlert(int orderId, boolean isBarOrder) {
        return false;
    }

    private void initOptions() {
        orderingOption = new ArrayList<>();
        orderingOption.add(new OrderingOption(1, getContext().getString(R.string.order_by_date), DbColumns.OrderEntry.COLUMN_NAME_LAST_MODIFIED, "DESC"));
        orderingOption.add(new OrderingOption(2, getContext().getString(R.string.order_by_place), DbColumns.OrderEntry.COLUMN_NAME_LAST_MODIFIED, "DESC"));
        orderingOption.add(new OrderingOption(3, getContext().getString(R.string.order_by_status), DbColumns.OrderEntry.COLUMN_NAME_LAST_MODIFIED, "DESC"));

        OrderingOption selected = this.orderingOption.get(0);
        setSelectedOption(spinnerOrdering, selected, selected.getName());
    }

    @NonNull
    @Override
    protected ListAdapterSource getListAdapterSource(ListAdapterSourceCallback<OrderPreparation> callback) {
        DataLoader<OrderPreparation> loader = new DataLoader<OrderPreparation>() {
            @Override
            protected Observable<OrderPreparation> getObservable(boolean forceRefresh) {
                return getOrderPreparationObservable();
            }
        };
        return new ListAdapterDataSource(callback, loader);
    }

    @NonNull
    protected Observable<OrderPreparation> getOrderPreparationObservable() {
        return getDataStorage().getOrderPreparations()
                .concatWith(Observable.from(new ArrayList<>()))
                .flatMapIterable(collection -> collection)
                .filter(orderPreparation -> orderPreparation.getOrder() != null &&
                        orderPreparation.getOrder().getCurrentOrderState() != OrderStates.OrderCreated ||
                        orderPreparation.isPaymentSuccess());
    }

    @Override
    protected ViewModelAdapter getAdapterInstance() {
        View.OnClickListener onPreview = view1 -> {
            OrderPreparation tag = (OrderPreparation) view1.getTag();
            if (tag.getOrder().isFinished() || tag.getOrder().isExpired()) {
                previewFinishedActivity(tag);
            } else {
                previewActiveOrder(tag);
            }
        };

        View.OnClickListener onDeleted = view1 -> {
            OrderPreparation tag = (OrderPreparation) view1.getTag();
            adapter.removeItem(tag);
        };
        String urlUsername = getPreferencesStorage().getAuthToken().getUrlUsername();
        String username = TextUtils.isEmpty(urlUsername) ?
                getString(R.string.unregistered_username) :
                urlUsername;

        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                add(OrderPreparation.class, OrderPreparationItemHolder.getLayout(),
                        view -> new OrderPreparationItemHolder(view, getProcessor(), getActivity(), username, onPreview, onDeleted));
            }
        });
    }

    private void previewFinishedActivity(OrderPreparation tag) {
        Intent intent = new Intent(getContext(), OrderPreparationPreviewActivity.class);
        intent.addFlags(IntentUtils.OVER_EXISTING);
        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, tag.getId());
        getActivity().startActivityForResult(intent, ActivityResults.ORDER_HISTORY_ITEM_PREVIEW_REQUEST);
    }

    private void previewActiveOrder(OrderPreparation tag) {
        Intent intent = OrderStatusActivity.getOrderPreviewActivity(getActivity(), tag.getId(), false);
        getActivity().startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityResults.ORDER_HISTORY_ITEM_PREVIEW_REQUEST &&
                resultCode == Activity.RESULT_CANCELED) {
            Logger.i(TAG, "Deleted, refresh list");
            refresh();
        }
    }

    @OnClick(R.id.spinner_history_ordering)
    public void selectOrdering() {
        Logger.i(TAG, "Select ordering...");

        selectOption(getString(R.string.dialog_title_select_ordering),
                null,
                orderingOption,
                spinnerOrdering,
                R.string.select_tip,
                ordering -> reorder(ordering),
                null);
    }

    private void reorder(OrderingOption ordering) {
        List<OrderPreparation> preparations = new ArrayList<>(adapter.getDataItems());
        Collections.sort(preparations, getComparator(ordering));
        adapter.replaceItems(preparations);
    }

    @NonNull
    private Comparator<OrderPreparation> getComparator(OrderingOption ordering) {
        if (ordering.getId() == 2) {
            return (o1, o2) -> o1.getPlace().getName().compareTo(o2.getPlace().getName());
        }
        if (ordering.getId() == 3) {
            return (o1, o2) -> o1.getOrder().getCurrentOrderState().getId() - o2.getOrder().getCurrentOrderState().getId();
        }
        return (o1, o2) -> (int)(dateToMillis(o1) - dateToMillis(o2));
    }

    private long dateToMillis(OrderPreparation o1) {
        try {
            return OrderPreparationItemHolder.FORMAT.parse(o1.getOrder().getTimestamp()).getTime();
        } catch (ParseException | NullPointerException e) {
            return 0;
        }
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_order_history;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public class OrderingOption extends NamedObject {

        private String orderKey;

        private String direction;

        public OrderingOption(int id, String name, String orderKey, String direction) {
            super(id, name);
            this.orderKey = orderKey;
            this.direction = direction;
        }
    }
}
