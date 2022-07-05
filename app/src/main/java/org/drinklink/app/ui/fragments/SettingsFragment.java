/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.DrinkLinkApplication;
import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.constants.PaymentParams;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.model.AuthorizationResponse;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.payment.InternationalGatewayPaymentManager;
import org.drinklink.app.persistence.model.SettingsPreferences;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.BillingAddressHolder;
import org.drinklink.app.ui.viewholder.CreditCardPreviewHolder;
import org.drinklink.app.utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import payment.sdk.android.cardpayment.CardPaymentData;

/**
 *
 */

public class SettingsFragment extends DrinkLinkFragment {

    private static final String TAG = "SettingsFragment";

    private static final int DELAY_5SECONDS_TO_GET_CARDS = 7000;

    @BindView(R.id.rg_message_sound_on)
    RadioButton stateChangeSoundOn;

    @BindView(R.id.rg_message_sound_off)
    RadioButton stateChangeSoundOff;

    @BindView(R.id.rg_ring_for_order_on)
    RadioButton readySoundOn;

    @BindView(R.id.rg_ring_for_order_off)
    RadioButton readySoundOff;

    @BindView(R.id.list_credit_cards)
    RecyclerView listCreditCads;

    ViewModelAdapter adapter;

    BillingAddressHolder billingAddressHolder;
    SettingsPreferences settingsPreferences;

    @OnClick(R.id.button_apply_settings)
    public void applySettings() {
        if (!billingAddressHolder.isValidOrEmptyEmail()) {
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_populate_billing_email),
                    getString(R.string.error_populate_billing_email));
            return;
        }
        persist();
        getActivity().finish();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        billingAddressHolder = new BillingAddressHolder(view, false, false);
        settingsPreferences = getPreferencesStorage().getSettingsPreferences();
        billingAddressHolder.bind(getContext(), 0, settingsPreferences);


        boolean readySoundOnValue = settingsPreferences.isReadySoundOn();
        readySoundOn.setChecked(readySoundOnValue);
        readySoundOff.setChecked(!readySoundOnValue);

        boolean stateChangeSoundOnValue = settingsPreferences.isStateChangeSoundOn();
        stateChangeSoundOn.setChecked(stateChangeSoundOnValue);
        stateChangeSoundOff.setChecked(!stateChangeSoundOnValue);

        adapter = getAdapter(settingsPreferences.getCards());
        listCreditCads.setAdapter(adapter);
    }

    private ViewModelAdapter getAdapter(List<CreditCardInfo> cards) {

        View.OnClickListener onClickListener = view -> {
            CreditCardInfo itemToDelete = (CreditCardInfo) view.getTag();
            showDialogBeforeDeletingCard(itemToDelete);
        };

        ViewModelAdapter adapter = new ViewModelAdapter(getContext(), cards, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(CreditCardInfo.class, CreditCardPreviewHolder.getLayout(), view ->
                        new CreditCardPreviewHolder(view, onClickListener));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    private void showDialogBeforeDeletingCard(CreditCardInfo itemToDelete) {
        DialogManager.showYesNoDialog(getActivity(),
                getString(R.string.remove_credit_card_title),
                getString(R.string.remove_credit_card_message),
                () -> {
                    SettingsPreferences settingsPreferences = getPreferencesStorage().getSettingsPreferences();
                    List<CreditCardInfo> cards = settingsPreferences.getCards();
                    cards.remove(itemToDelete);
                    getPreferencesStorage().save(settingsPreferences);
                    adapter.replaceItems(cards);
                }, () -> {
                });
    }

    private void persist() {
        billingAddressHolder.captureValues();
        SettingsPreferences updatedSettings = billingAddressHolder.getItem();
        SettingsPreferences settingsPreferences = getPreferencesStorage().getSettingsPreferences();
        settingsPreferences.setReadySoundOn(readySoundOn.isChecked());
        settingsPreferences.setStateChangeSoundOn(stateChangeSoundOn.isChecked());
        settingsPreferences.setEmail(updatedSettings.getEmail());
        settingsPreferences.setBillToCardHolder(updatedSettings.isBillToCardHolder());
        settingsPreferences.setFirstName(updatedSettings.getFirstName());
        settingsPreferences.setLastName(updatedSettings.getLastName());
        settingsPreferences.setFullAddress(updatedSettings.getFullAddress());

        getPreferencesStorage().save(settingsPreferences);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_settings;
    }

    @OnClick(R.id.btn_add_card)
    public void addCard() {

        if (!billingAddressHolder.isValidEmail()) {
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_populate_billing_email),
                    getString(R.string.error_populate_billing_email));
            return;
        } else if (!billingAddressHolder.isBillingValid()) {
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_populate_billing),
                    getString(R.string.error_populate_billing_details));
            return;
        }

        dialog = DialogManager.showYesNoDialog(getActivity(),
                getString(R.string.add_credit_card_title),
                getString(R.string.add_credit_card_message),
                () -> {
                    String username = getPreferencesStorage().getAuthToken().getUrlUsername();
                    if (username == null) {
                        //trigger automatic sign-up as user name is null
                        ActionCallback<AuthorizationResponse> callback = new ActionCallback<AuthorizationResponse>(progressBar, getActivity()) {
                            @Override
                            public void onSuccess(AuthorizationResponse body) {
                            }

                            @Override
                            protected void onError(int code, String message, String errorBody) {
                                Logger.i(TAG, "failed request for unauthorized user, retry after automatic sign up");
                                addSavedCardsApi();
                            }
                        };
                        String fakeUserName = "fake";
                        getApiCalls().addSavedCard().enqueue(trackCallback(callback));
                    } else {
                        addSavedCardsApi();
                    }
                }, () -> {});
    }

    private void addSavedCardsApi() {
        String username = getPreferencesStorage().getAuthToken().getUrlUsername();
        ActionCallback<AuthorizationResponse> callback = new ActionCallback<AuthorizationResponse>(progressBar, getActivity()) {
            @Override
            public void onSuccess(AuthorizationResponse body) {
                startActivityForResult(InternationalGatewayPaymentManager.startPayment(body, getActivity()), PaymentParams.PAYMENT_REQUEST_CODE);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                Logger.i(TAG, message);
                showErrorBody(errorBody);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
            }
        };
        getApiCalls().addSavedCard().enqueue(trackCallback(callback));
    }

    private void showErrorBody(String errorBody) {
        if (!TextUtils.isEmpty(errorBody)) {
            dialog = DialogManager.showOkDialog(getActivity(),
                    getString(R.string.error_an_error), errorBody);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PaymentParams.PAYMENT_REQUEST_CODE) {
            processNetworkInternationalResult(resultCode, data);
        }
    }

    private void processNetworkInternationalResult(int resultCode, Intent data) {
        if (resultCode == ExtrasKey.RESULT_OK) {
            CardPaymentData paymentData = CardPaymentData.getFromIntent(data);
            int code = paymentData.getCode();

            if (code == CardPaymentData.STATUS_PAYMENT_AUTHORIZED ||
                code == CardPaymentData.STATUS_PAYMENT_CAPTURED) {
                progressBar.increase();
                onCardAuthorized(8);
            } else {
                Toast.makeText(getContext(), "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getContext(), "Payment canceled", Toast.LENGTH_LONG).show();
        }
    }

    private void onCardAuthorized(final int retry) {
        String username = getPreferencesStorage().getAuthToken().getUrlUsername();

        if (username == null) {
            DrinkLinkApplication.getComponent().getInterceptor().refreshToken("");
        }
        getApiCalls().getSavedCreditCards().enqueue(new ActionCallback<List<CreditCardInfo>>(ProgressBarCounter.NO_PROGRESS_BAR, getActivity()) {
            @Override
            public void onSuccess(List<CreditCardInfo> cards) {
                SettingsPreferences settingsPreferences = getPreferencesStorage().getSettingsPreferences();
                Logger.i(TAG, "Number of cards: " + cards.size() + ", retries: " + retry);
                if (cards.isEmpty()) {
                    if (retry > 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> onCardAuthorized(retry - 1), DELAY_5SECONDS_TO_GET_CARDS);
                    } else {
                        progressBar.decrease();
                        dialog = DialogManager.showOkDialog(getActivity(),
                                getString(R.string.error_an_error),
                                getString(R.string.error_an_error_occurred));
                    }
                    return;
                }
                progressBar.decrease();

                Set<CreditCardInfo> cardsSet = new HashSet<>();
                CreditCardInfo.mergeCards(cardsSet, settingsPreferences.getCards());
                List<CreditCardInfo> newlyAdded = CreditCardInfo.mergeCards(cardsSet, cards);
                for (CreditCardInfo newCard: newlyAdded) {
                    billingAddressHolder.setCardBillingAddress(newCard, newCard.getCardholderName());
                }

                Logger.i(TAG, "Number of unique cards" + cardsSet.size());
                ArrayList<CreditCardInfo> mergedCardsList = new ArrayList<>(cardsSet);
                settingsPreferences.setCards(mergedCardsList);
                getPreferencesStorage().save(settingsPreferences);
                adapter.replaceItems(mergedCardsList);

                getApiCalls().deleteSavedCards().enqueue(new ActionCallback<Void>(ProgressBarCounter.NO_PROGRESS_BAR, null) {
                    @Override
                    protected void onError(int code, String message, String errorBody) {
                    }
                });
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                progressBar.decrease();
                Logger.i(TAG, "get saved card failed");
                dialog = DialogManager.showYesNoDialog(getActivity(),
                        getString(R.string.error_an_error),
                        getString(R.string.error_ok_to_retry),
                        () -> {
                            progressBar.increase();
                            onCardAuthorized(retry - 1);
                        }, () -> { });
            }
        });
    }
}
