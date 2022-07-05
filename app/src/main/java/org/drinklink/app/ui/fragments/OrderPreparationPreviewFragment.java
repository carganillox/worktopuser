/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.activities.MainActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.CodeAndStatusPreviewHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemBaseHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemHolder;
import org.drinklink.app.ui.viewholder.DrinkListItemSelectionHolder;
import org.drinklink.app.ui.viewholder.OrderPreparationItemPreview;
import org.drinklink.app.ui.viewmodel.SelectedDrinkPreviewItem;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.workflow.IOrderProcessor;
import org.drinklink.app.workflow.OrderProcessor;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */

public class OrderPreparationPreviewFragment extends DrinkLinkFragment {

    private ViewModelAdapter adapter;
    private CodeAndStatusPreviewHolder codeAndStatusHolder; // nullable time to collect
    private OrderPreparationItemPreview orderPreparationItemPreview;

    IOrderProcessor orderProcessor;
    OrderPreparation orderPreparation;

    @BindView(R.id.main_list)
    RecyclerView list;

    @BindView(R.id.button_preview_delete)
    Button btnDelete;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_order_preparation_preview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        codeAndStatusHolder = new CodeAndStatusPreviewHolder(view);
        String urlUsername = getPreferencesStorage().getAuthToken().getUrlUsername();
        String username = TextUtils.isEmpty(urlUsername) ?
                getString(R.string.unregistered_username) :
                urlUsername;

        View.OnClickListener onDeleted = view1 -> {};
        orderPreparationItemPreview = new OrderPreparationItemPreview(view, orderProcessor, getActivity(), username, clickView -> {}, onDeleted);
        bind();
    }

    private void bind() {
        if (orderProcessor != null) {
            codeAndStatusHolder.bind(getContext(), 0, orderProcessor);
            orderPreparationItemPreview.bind(getContext(), 0, orderPreparation);
            showDrinkList();
//            btnDelete.setEnabled(orderPreparation.getOrder().isFinished());
        }
    }

    private void showDrinkList() {
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = getAdapter();
        list.setAdapter(adapter);
    }

    private ViewModelAdapter getAdapter() {
        DrinkListItemBaseHolder.CountTracker countTracker = new DrinkListItemBaseHolder.CountTracker();
        final IOrderProcessor processor = orderProcessor;
        List<SelectedDrinkPreviewItem> orderItems = ListUtil.transform(processor.getOrderItems(), (i) -> new SelectedDrinkPreviewItem(i));
        ViewModelAdapter adapter = new ViewModelAdapter(getContext(), orderItems, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(SelectedDrinkPreviewItem.class, DrinkListItemHolder.getPreviewLayout(), view ->
                        new DrinkListItemSelectionHolder(view, processor, getActivity(), countTracker));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    @Override
    public void onDestroyView() {
        codeAndStatusHolder.unBind();
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
            orderPreparation = getDataStorage().getOrderPreparation(orderId);
            if (orderPreparation != null) {
                orderProcessor = new OrderProcessor(orderPreparation);
            }
        }
    }

    @OnClick(R.id.button_repeat_order_preview)
    public void repeatOrder(View view) {

        IOrderProcessor mainProcessor = getProcessor();
        mainProcessor.reset();
        mainProcessor.forPlace(orderPreparation.getPlace());
        mainProcessor.merge(new OrderProcessor(new OrderPreparation(orderPreparation)));
        mainProcessor.save();

        Context ctx = getContext();
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(IntentUtils.CLEAR_AND_NEW);
        intent.putExtra(ExtrasKey.PLACE_ID_EXTRA, orderPreparation.getPlace().getId());
        intent.putExtra(ExtrasKey.SHOW_STATUS, false);
        ctx.startActivity(intent);
    }

    @OnClick(R.id.button_preview_delete)
    public void deleteOrder(View view) {
        if (orderPreparation.getOrder().isFinished() ||
            orderPreparation.getOrder().isExpired()) {
            dialog = DialogManager.showYesNoDialog(getActivity(),
                    getString(R.string.delete_order_title),
                    getString(R.string.delete_order_message),
                    () -> {
                        getProcessor().deleteOrderPreparation(orderPreparation.getId());
                        getActivity().setResult(Activity.RESULT_CANCELED);
                        getActivity().finish();
                    }, () -> {
                    });
        } else {
            dialog = DialogManager.showOkDialog(getActivity(),
                    getString(R.string.delete_order_title),
                    getString(R.string.delete_order_pending));
        }
    }
}
