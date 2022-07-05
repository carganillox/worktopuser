/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.OrderUpdateListenerFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.exception.OrderNotFound;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.request.OrderCancellation;
import org.drinklink.app.model.request.OrderResponse;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.activities.CodeAndStatusActivity;
import org.drinklink.app.ui.activities.MainActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.CountDownHolder;
import org.drinklink.app.ui.viewholder.PlaceHeaderHolder;
import org.drinklink.app.ui.viewholder.StateItemHolder;
import org.drinklink.app.ui.viewmodel.StateItem;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static org.drinklink.app.persistence.model.OrderPreparation.NOT_PUBLISHED_ORDER_ID;

/**
 *
 */

public class OrderStatusFragment extends OrderUpdateListenerFragment {

    private static final String TAG = "OrderStatusFragment";

    private PlaceHeaderHolder placeHeaderHolder;

    @BindView(R.id.list_states)
    RecyclerView list;

    @BindView(R.id.header_code)
    TextView tvCode;

    @BindView(R.id.button_collection_point)
    Button btnCollectionPoint;

    @BindView(R.id.btn_back_to_order)
    Button btnBackToOrder;

    @BindView(R.id.btn_new_order)
    Button btnNewOrder;

    @BindView(R.id.time_to_collect_container)
    View timeToCollectContainer;

    @BindView(R.id.lbl_time_to_collect)
    View lblTimeToCollect;

    @BindView(R.id.timer_description)
    View timerDescription;

    @BindView(R.id.click_at_collection_margin)
    View collectionPointMargin;

    private Handler handler = new Handler(Looper.myLooper());
    ViewModelAdapter adapter;
    ArrayList<StateItem> stateItems;
    private int requestedOrderId;
    IOrderProcessorPreview orderProcessorPreview;
    CountDownHolder countDownHolder;

    private StateItem collect;
    private StateItem pendingEntryState;
    private boolean isAlert;
    private boolean canGoBack;

    @NonNull
    private ArrayList<StateItem> getStateItems(long id) {
        stateItems = new ArrayList<StateItem>() {
            {
                pendingEntryState = new StateItem.PendingStateItem(getContext(), OrderStates.Pending, true, getCurrentOrderProcessor());
                add(pendingEntryState);
                StateItem acceptedOrRejected = new StateItem.DoubleState(
                        new StateItem(getString(R.string.state_item_order_accepted), OrderStates.Processed),
                        new StateItem(getString(R.string.state_item_order_rejected), OrderStates.Rejected),
                        new StateItem(getString(R.string.state_item_order_payment_failed), OrderStates.PaymentFailed),
                        new StateItem(getString(R.string.state_item_order_canceled), OrderStates.Canceled));

                add(acceptedOrRejected);
                StateItem processed = new StateItem(getString(R.string.state_item_payment_processed), OrderStates.Processed);
                add(processed);
                StateItem preparing = new StateItem(getString(R.string.state_item_preparing_order), OrderStates.Accepted);
                add(preparing);
                int collectOrderRes = isTable() ?
                        R.string.state_item_order_on_the_way :
                        R.string.state_item_collect_order;
                collect = new StateItem(getString(collectOrderRes),
                        OrderStates.Ready,
                        OrderStates.Collected,
                        OrderStates.NotCollected);
                collect.setTable(isTable());
                collect.setHasDiscount(getOrder().getDiscount() != null);
                updateBar(getOrder());
                add(collect);
                pendingEntryState.setNext(acceptedOrRejected).setNext(processed).setNext(preparing).setNext(collect);
            }
        };
        return stateItems;
    }

    @Override
    public void onResume() {
        super.onResume();
        countDownHolder.setResumed(true);
        Order order = getOrder();
        updateOrderPreview(order);
        onOrderUpdated(order, order.getCurrentOrderState());
        if (isAlert) {
            isAlert = false;
            onOrderAlert(getOrderId(), order.isBarOrder());
        }
        reloadOrder();
    }

    // reload order on resume to check for changes in case there was an issue with notifications
    private void reloadOrder() {
        getApiCalls().getOrder(requestedOrderId).enqueue(trackCallback(new ActionCallback<OrderResponse>(ProgressBarCounter.NO_PROGRESS_BAR, null) {
            @Override
            public void onSuccess(OrderResponse order) {
                Logger.i(TAG, "reloadOrder, newState: " + order.getCurrentOrderState() + ", currentState: " +  getOrder().getCurrentOrderState());
                updateRefreshedOrder(order);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {

            }
        }));
    }

    private void updateRefreshedOrder(Order order) {
        OrderPreparation matchingOrderPreparation = getProcessor().getMatchingOrderPreparation(order.getId());
        if (matchingOrderPreparation == null) {
            Logger.i(TAG, "matching order not found:" + order.getId());
            return;
        }
        OrderStates previousOrderState = getProcessor().updateCurrentOrder(order, matchingOrderPreparation);
        getProcessor().updateOrder(order, previousOrderState);
    }

    @Override
    public void onPause() {
        handler.removeCallbacksAndMessages(null);
        countDownHolder.setResumed(false);
        super.onPause();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_order_status;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        placeHeaderHolder = new PlaceHeaderHolder(view);
        placeHeaderHolder.bind(getContext(), 0, getPlace());
        countDownHolder = new CountDownHolder(view, R.color.white, () -> getProcessor().updateOrder(getOrder(), getOrder().getCurrentOrderState()));
        countDownHolder.bind(getContext(), 0, getCurrentOrderProcessor());

        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(requestedOrderId);
        initOrder();
        bindTable();
    }

    private void bindTable() {
        boolean isTable = isTable();
        boolean isFinished = getOrder().getCurrentOrderState().getId() > OrderStates.Ready.getId();
        btnCollectionPoint.setText(getString(isTable ?
                R.string.btn_click_when_order_arrives   :
                R.string.btn_click_at_collection_point));
        setVisibility(timeToCollectContainer,!isTable || isFinished);
        setVisibility(lblTimeToCollect, !isTable);
        setVisibility(timerDescription, !isTable);
        setVisibility(collectionPointMargin, isTable);
    }

    private boolean isTable() {
        return !getOrder().isBarOrder();
    }

    private void orderUpdated(Order order) {
        // handle notifications
        try {
            getProcessor().updateOrder(order, OrderStates.Pending);
        } catch (OrderNotFound orderNotFound) {
            Logger.i(TAG, "order not found" + orderNotFound.getOrderId());
        }
    }

    private void refreshOrder(Order order) {
        OrderStates state = order.getCurrentOrderState();
        Logger.d(TAG, "State: " + state);

        StateItem updatedItem = pendingEntryState.setState(state);
        updateOrderPreview(order);

        boolean rejected = isRejected(state);
        if (updatedItem != null && rejected) {
            setRejectedOrCanceled();
            updatedItem.setRejected(order.getBartender(), order.getReason(), order.getAdditionalInfo());
        } else if (updatedItem != null && state == OrderStates.Canceled) {
            pendingEntryState.setCancelable(false);
            setRejectedOrCanceled();
            updatedItem.setRejected(null, null, null);
        } else if (rejected) { // special case: rejected while state in unexpected order
            pendingEntryState.getNext().setRejected(null, null, null);

        }

        order.captureLastModified();

        adapter.notifyDataSetChanged();
        return;
    }

    private boolean isRejected(OrderStates state) {
        return state == OrderStates.Rejected || state == OrderStates.PaymentFailed;
    }

    private void updateOrderPreview(Order order) {
        updateBar(order);
        updateCode(order);
//        updateQueueSize();
    }

    private void updateBar(Order order) {
        if (order.getBar() != null) {
            collect.setBar(order.getBar());
            return;
        }
        if (order.getBarId() != null) {
            Bar bar = getPlace().getBar(order.getBarId());
            if (bar != null) {
                getCurrentOrderProcessor().setBar(bar);
                collect.setBar(bar);
            }
        }
    }

    private void setRejectedOrCanceled() {
        countDownHolder.setRejectedOrCanceled();
        setVisibility(btnBackToOrder, true);
        setVisibility(btnCollectionPoint, false);
        setVisibility(btnNewOrder, false);
    }

    private void updateCode(Order order) {
        String code = order.getCode();
        tvCode.setText(code == null ? getString(R.string.code_placeholder) : getString(R.string.code_header_format, code));
    }

    private Order getOrder() {
        return getCurrentOrderProcessor().getOrder();
    }

    private IOrderProcessorPreview getCurrentOrderProcessor() {
        if (orderProcessorPreview == null) {
            orderProcessorPreview = getProcessor();
        }
        return orderProcessorPreview;
    }

    private void initOrder() {
        list.setAdapter(getAdapter());
    }

    private RecyclerView.Adapter getAdapter() {

        adapter = new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                View.OnClickListener onClick = (view) -> cancelOrder();
                add(StateItem.class, StateItemHolder.getLayout(), view -> new StateItemHolder(view, onClick));
                add(StateItem.DoubleState.class, StateItemHolder.getLayout(), view -> new StateItemHolder(view, onClick));
                add(StateItem.PendingStateItem.class, StateItemHolder.getLayout(), view -> new StateItemHolder(view, onClick));
            }
        });
        stateItems = getStateItems(getOrderId());
        adapter.appendItems(stateItems);
        return adapter;
    }

    private void cancelOrder() {
        Logger.d(TAG, "Cancel order by user");
        ActionCallback<Order> callback = new ActionCallback<Order>(progressBar, getActivity()) {
            @Override
            public void onSuccess(Order order) {
                updateRefreshedOrder(order);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                showToast(message);
                showErrorBody(errorBody);
            }
        };
        getApiCalls().cancel(getOrderId(), OrderCancellation.INSTANCE).enqueue(trackCallback(callback));
    }

    private void showErrorBody(String errorBody) {
        if (!TextUtils.isEmpty(errorBody)) {
            dialog = DialogManager.showOkDialog(getActivity(),
                    getString(R.string.error_an_error), errorBody);
        }
    }

    @Override
    public void onDestroyView() {
        placeHeaderHolder.unBind();
        super.onDestroyView();
    }

    @OnClick({ R.id.button_collection_point})
    public void submit(View view) {
        Intent intent = new Intent(getContext(), CodeAndStatusActivity.class);
        intent.addFlags(IntentUtils.OVER_EXISTING);
        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, getOrderId());
        startActivity(intent);
    }

    private int getOrderId() {
        return getCurrentOrderProcessor().getId();
    }

    @OnClick({ R.id.btn_back_to_order})
    public void nextOnCancelOrRejected(View view) {
        resetProcessor();
        getProcessor().merge(getCurrentOrderProcessor());
        startNewOrder();
    }

    @OnClick({ R.id.btn_new_order})
    public void onNewOrder(View view) {
        resetProcessor();
        startNewOrder();
    }

    private void resetProcessor() {
        getProcessor().reset();
        getProcessor().forPlace(getPlace());
    }

    private void startNewOrder() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(IntentUtils.CLEAR_AND_NEW);
        intent.putExtra(ExtrasKey.PLACE_ID_EXTRA, getPlace().getId());
        startActivity(intent);
    }

    private Place getPlace() {
        return getCurrentOrderProcessor().getPlace();
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        requestedOrderId = bundle.getInt(ExtrasKey.ORDER_ID_EXTRA, NOT_PUBLISHED_ORDER_ID);
        canGoBack = bundle.getBoolean(ExtrasKey.CAN_GO_BACK_EXTRA, false);
        boolean showAlert = bundle.getBoolean(ExtrasKey.SHOW_ORDER_READY_ALERT_EXTRA, false);
        Logger.i(TAG, "show orderId:" + requestedOrderId);
        if (requestedOrderId != NOT_PUBLISHED_ORDER_ID) {
            orderProcessorPreview = getProcessor().findOrderProcessor(requestedOrderId);
            getProcessor().setLastActive(requestedOrderId);
            isAlert = showAlert;
        } else {
            String placeString = bundle.getString(ExtrasKey.PLACE_EXTRA);
            if (placeString != null) {
                Place place = getGson().fromJson(placeString, Place.class);
                getProcessor().forPlace(place);
            }
        }
        Logger.i(TAG, "order: " + getCurrentOrderProcessor().getId());
    }

    @Override
    public boolean onBackPress() {
        // consume back press without doing anything
        return !canGoBack;
    }

    @Override
    public boolean onOrderUpdated(Order order, OrderStates previousState) {
        super.onOrderUpdated(order, previousState);
        refreshOrder(order);
        countDownHolder.initTimeToCollect(order);
        bindTable();
        return true;
    }

    @Override
    public boolean isMatch(int orderId) {
        return getOrderId() == orderId;
    }
}
