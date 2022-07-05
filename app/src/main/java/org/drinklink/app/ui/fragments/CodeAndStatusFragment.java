/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.OrderUpdateListenerFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.viewholder.CodeAndStatusHolder;
import org.drinklink.app.ui.viewholder.CountDownHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemBaseHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemSelectionHolder;
import org.drinklink.app.ui.viewmodel.SelectedDrinkPreviewItem;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.util.List;

import butterknife.BindView;

/**
 *
 */

public class CodeAndStatusFragment extends OrderUpdateListenerFragment {

    private ViewModelAdapter adapter;
    private CodeAndStatusHolder codeAndStatusHolder;
    private CountDownHolder countDownHolder;

    IOrderProcessorPreview orderProcessor;

    @BindView(R.id.main_list)
    RecyclerView list;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_code_and_status;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        codeAndStatusHolder = new CodeAndStatusHolder(view);
        countDownHolder = new CountDownHolder(view, R.color.mango_tango,
                () -> getProcessor().updateOrder(getOrder(), getOrder().getCurrentOrderState()));
        if (orderProcessor != null) {
            codeAndStatusHolder.bind(getContext(), 0, orderProcessor);
            countDownHolder.bind(getContext(), 0, orderProcessor);
            showDrinkList();
        }
    }

    private Order getOrder() {
        return orderProcessor.getOrder();
    }

    private void showDrinkList() {
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = getAdapter();
        list.setAdapter(adapter);
    }

    private ViewModelAdapter getAdapter() {
        final IOrderProcessorPreview processor = orderProcessor;
        DrinkListItemBaseHolder.CountTracker countTracker = new DrinkListItemBaseHolder.CountTracker();
        List<SelectedDrinkPreviewItem> orderItems = ListUtil.transform(processor.getOrderItems(), (i) -> new SelectedDrinkPreviewItem(i));
        ViewModelAdapter adapter = new ViewModelAdapter(getContext(), orderItems, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(SelectedDrinkPreviewItem.class, DrinkListItemHolder.getCodeAndStatusLayout(), view ->
                        new DrinkListItemSelectionHolder(view, processor, getActivity(), countTracker));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    @Override
    public void onDestroyView() {
        codeAndStatusHolder.unBind();
        countDownHolder.unBind();
        super.onDestroyView();
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        if (bundle == null) {
            orderProcessor = getProcessor();
            return;
        }
        int defaultId = OrderPreparation.NOT_PUBLISHED_ORDER_ID;
        int orderId = bundle.getInt(ExtrasKey.ORDER_ID_EXTRA, defaultId);
        if (orderId != defaultId) {
            orderProcessor = getProcessor().findOrderProcessor(orderId);
        }
    }

    @Override
    public boolean onOrderUpdated(Order order, OrderStates previousState) {
        super.onOrderUpdated(order, previousState);
        codeAndStatusHolder.bind(getContext(), 0, orderProcessor);
        countDownHolder.bind(getContext(), 0, orderProcessor);
        return true;
    }

    @Override
    public boolean isMatch(int orderId) {
        return orderProcessor.getId() == orderId;
    }

    @Override
    public void onResume() {
        super.onResume();
        countDownHolder.setResumed(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownHolder.setResumed(false);
    }
}
