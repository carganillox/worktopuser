/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.contract.ListAdapterDataSource;
import org.drinklink.app.common.contract.ListAdapterSource;
import org.drinklink.app.common.contract.ListAdapterSourceCallback;
import org.drinklink.app.common.fragment.CommonListFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.DataLoader;
import org.drinklink.app.model.IdObject;
import org.drinklink.app.ui.viewholder.CreditCardItemHolder;
import org.drinklink.app.ui.viewholder.LabeltemHolder;
import org.drinklink.app.ui.viewholder.OrderSummaryInfoHolder;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.ui.viewmodel.LabelItem;
import org.drinklink.app.utils.ListUtil;

import butterknife.OnClick;
import rx.Observable;

/**
 *
 */

public class EnterCreditCardFragment extends CommonListFragment<IdObject> {

    private OrderSummaryInfoHolder orderHolder;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderHolder = new OrderSummaryInfoHolder(view);
        orderHolder.bind(getContext(), 0, getProcessor());
    }

    @Override
    public void onDestroyView() {
        orderHolder.unBind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        orderHolder.register();
    }

    @Override
    public void onPause() {
        orderHolder.unregister();
        super.onPause();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_enter_credit_card;
    }

    @NonNull
    @Override
    protected ListAdapterSource getListAdapterSource(final ListAdapterSourceCallback<IdObject> callback) {
        DataLoader<IdObject> loader  = new DataLoader<IdObject>() {
            @Override
            protected Observable<IdObject> getObservable(boolean forceRefresh) {
                return Observable.from(ListUtil.<IdObject>asList(new LabelItem(getString(R.string.lbl_favorite_cards))))
                        .concatWith(getSavedCreditCards())
                        .concatWith(Observable.from(ListUtil.<IdObject>asList(new CreditCardInfo())));
            }
        };
        return new ListAdapterDataSource<>(callback, loader);
    }

    private Observable<CreditCardInfo> getSavedCreditCards() {
        CreditCardInfo item = new CreditCardInfo();
        item.setName("My Visa");
        return Observable.from(ListUtil.asList(item));
    }

    @Override
    protected ViewModelAdapter getAdapterInstance() {
        return new ViewModelAdapter(getContext(), new ViewModelHolderFactory() {
            {
                View.OnClickListener onClick = view -> {
                    CreditCardInfo card = (CreditCardInfo)view.getTag();
                    showToast("card selected: " + card.getName());
                };

                add(LabelItem.class, LabeltemHolder.getLayoutSelectCard(), view -> new LabeltemHolder(view));
                add(CreditCardInfo.class, CreditCardItemHolder.getLayout(),
                        view -> new CreditCardItemHolder(view, onClick));
            }
        });
    }

    @OnClick(R.id.btn_pay)
    void pay() {
        showToast("processing payment...");
    }
}
