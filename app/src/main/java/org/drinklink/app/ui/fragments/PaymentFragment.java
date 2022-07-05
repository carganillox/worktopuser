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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.drinklink.app.R;
import org.drinklink.app.common.adapter.ViewModelAdapter;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.constants.PaymentParams;
import org.drinklink.app.common.fragment.DrinkLinkFragment;
import org.drinklink.app.common.viewholder.ViewModelHolderFactory;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.model.BillingAddress;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.Discount;
import org.drinklink.app.model.NamedObject;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.Place;
import org.drinklink.app.model.Table;
import org.drinklink.app.model.Tip;
import org.drinklink.app.model.request.OrderRequest;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.persistence.model.SettingsPreferences;
import org.drinklink.app.ui.activities.OrderStatusActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.BillingAddressHolder;
import org.drinklink.app.ui.viewholder.CreditCardUsageHolder;
import org.drinklink.app.ui.viewholder.PlaceHeaderHolder;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.utils.MoneyUtils;
import org.drinklink.app.utils.OpenHoursData;
import org.drinklink.app.utils.TimeUtils;
import org.drinklink.app.workflow.IOrderProcessor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import payment.sdk.android.cardpayment.CardPaymentData;
import payment.sdk.android.cardpayment.CardPaymentFragment;
import payment.sdk.android.cardpayment.SavedCardPayment;

import static org.drinklink.app.utils.MoneyUtils.notZero;

/**
 *
 */
public class PaymentFragment extends DrinkLinkFragment {

    private static String TAG = "PaymentFragment";
    private static final int CHECK_SAVED_CARDS_DELAY = 5000;
    String ORDER_IN_PAYMENT = "orderInPayment";

    private Place place;
    private PlaceHeaderHolder placeHeaderHolder;
    private SettingsPreferences settingsPreferences;
    private BillingAddressHolder billingAddressHolder;

    private static final ArrayList<Tip> TIPS = new ArrayList<Tip>() {
        {
            add(new Tip("5% tip", 1, new BigDecimal(5)));
            add(new Tip("10% tip", 2, new BigDecimal(10)));
            add(new Tip("15% tip", 3, new BigDecimal(15)));
            add(new Tip("20% tip", 4, new BigDecimal(20)));
            add(new Tip("5 AED", 5, null, new BigDecimal(5), "Tip"));
            add(new Tip("10 AED", 6, null, new BigDecimal(10), "Tip"));
            add(new Tip("15 AED", 7, null, new BigDecimal(15), "Tip"));
            add(new Tip("20 AED", 8, null, new BigDecimal(20), "Tip"));
        }
    };

    @BindView(R.id.tv_order_amount_original)
    TextView tvAmount;

    @BindView(R.id.tv_order_original)
    TextView tvOrder;

    @BindView(R.id.tv_order_amount_total)
    TextView tvAmountTotal;

    @BindView(R.id.tv_discount_amount)
    TextView tvDiscountAmount;

    @BindView(R.id.tv_discount_amount_aed)
    TextView tvDiscountAmountAed;

    @BindView(R.id.spinner_discount)
    AppCompatButton spinnerDiscount;

    @BindView(R.id.tv_tip_amount)
    TextView tvTipAmount;

    @BindView(R.id.tv_tip_amount_aed)
    TextView tvTipAmountAed;

    @BindView(R.id.spinner_tip)
    AppCompatButton spinnerTip;

    @BindView(R.id.spinner_table)
    AppCompatButton spinnerTable;

    @BindView(R.id.button_pay)
    AppCompatButton btnPay;

    @BindView(R.id.switch_at_bar)
    RadioButton switchAtBar;

    @BindView(R.id.switch_at_table)
    RadioButton switchAtTable;

    @BindView(R.id.table_container)
    View containerTable;

    @BindView(R.id.tv_card_holder)
    TextView tvCardHolder;

    @BindView(R.id.et_card_holder)
    EditText etCardHolder;

    @BindView(R.id.tv_valid_until)
    TextView tvValidUntil;

    @BindView(R.id.et_valid_until)
    EditText etValidUntil;

    @BindView(R.id.tv_card_no)
    TextView tvCardNo;

    @BindView(R.id.et_card_no)
    EditText etCardNo;

    @BindView(R.id.tv_ccv_code)
    TextView tvCcvCode;

    @BindView(R.id.et_ccv_code)
    EditText etCcvCode;

    @BindView(R.id.tv_vip_amount_container)
    View vipContainer;

    @BindView(R.id.vip_switch)
    SwitchCompat vipSwitch;

    @BindView(R.id.tv_vip_amount)
    TextView tvVipAmount;

    @BindView(R.id.tv_vip_amount_aed)
    TextView tvVipAmountAed;

    @BindView(R.id.tv_service_charge_amount)
    TextView tvServiceChargeAmount;

    @BindView(R.id.service_charge_container)
    View tvServiceChargeContainer;

    @BindView(R.id.check_box_save_card)
    CheckBox saveCardCheckBox;

    @BindView(R.id.list_credit_cards)
    RecyclerView listCreditCads;

    @BindView(R.id.saved_cards_container)
    View savedCardsContainer;

    @BindView(R.id.tv_saved_cards)
    View tvSelectCard;

    @BindView(R.id.saved_card_border)
    View borderSavedCard;

    @BindView(R.id.payment_fragment_container)
    View cardFragmentContainer;

    @BindView(R.id.btn_vip_info_container)
    View vipInfoContainer;

    @BindView(R.id.btn_saved_card_info_container)
    View savedCardInfoContainer;

    @BindView(R.id.check_box_save_billing_info)
    CheckBox cbSaveBilling;

    @BindView(R.id.tip_spinner_underline)
    View tipSpinnerUnderline;

    ViewModelAdapter adapter;

    CardPaymentFragment cardPaymentFragment;
    private int orderId = -1;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_payment;
    }

    @Override
    protected FragmentActivity getActivityForProgressBar() {
        // return null activity so that progress bar will not block UI
        return getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            orderId = savedInstanceState.getInt(ORDER_IN_PAYMENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
        outState.putInt(ORDER_IN_PAYMENT, orderId);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        placeHeaderHolder = new PlaceHeaderHolder(view);
        placeHeaderHolder.bind(getContext(), 0, place);
        billingAddressHolder = new BillingAddressHolder(view, true, true);

        initPaymentFragment();

        initOrder();

        bindEditTextWithLabelVisibility(etCardHolder, tvCardHolder);
        bindEditTextWithLabelVisibility(etValidUntil, tvValidUntil);
        bindEditTextWithLabelVisibility(etCardNo, tvCardNo);
        bindEditTextWithLabelVisibility(etCcvCode, tvCcvCode);

        bindVipCharge();
        bindServiceChange();

        bindSpinnersDrawableRight(spinnerDiscount, spinnerTip, spinnerTable);

        saveCardCheckBox.setChecked(false);
    }

    private void bindBillingAddress() {
        billingAddressHolder.bind(getContext(), 0, settingsPreferences);
        billingAddressHolder.setVisibilityAccordingToAddress();
        if (billingAddressHolder.isBillingValid()) {
            setVisibility(cbSaveBilling, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsPreferences = getPreferencesStorage().getSettingsPreferences();
        bindCreditCards();
        bindBillingAddress();
    }

    private void bindCreditCards() {
        List<CreditCardInfo> cards = settingsPreferences.getCards();
        adapter = getAdapter(cards);
        listCreditCads.setAdapter(adapter);
        setVisibility(savedCardsContainer, !cards.isEmpty());
    }

    private void initPaymentFragment() {
        cardPaymentFragment = new CardPaymentFragment(result -> processNetworkInternationalResult(result));
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.payment_fragment_container, cardPaymentFragment);
        transaction.commit();
    }

    private void bindVipCharge() {
        boolean hasVipCharge = MoneyUtils.greaterThanZero(place.getVipOrderCharge());
        setVisibility(vipContainer, hasVipCharge);
        setVisibility(tipSpinnerUnderline, hasVipCharge);
        vipSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            getProcessor().setVipCharge(checked ? place.getVipOrderCharge() : BigDecimal.ZERO);
            recalculate(getProcessor());
        });
        vipSwitch.setText(getContext().getString(R.string.vip_charge_button_format, place.getVipOrderCharge()));

        vipInfoContainer.setOnClickListener(view -> vipInfo());
        savedCardInfoContainer.setOnClickListener(view -> savedCardInfo());
    }

    private void bindServiceChange() {
        boolean isServiceChangeEnabled = isServiceChangeEnabled();
        setVisibility(tvServiceChargeContainer, isServiceChangeEnabled);
    }

    private boolean isServiceChangeEnabled() {
        return place.isServiceChargeEnabled() && MoneyUtils.greaterThanZero(place.getServiceChargePercentage());
    }

    private void initOrder() {
        IOrderProcessor processor = getProcessor();

        tvAmount.setText(getContext().getString(R.string.order_amount_format, processor.sumPlainDrinkWithMixerPrice()));
        int count = processor.getCount();
        String plural = count == 1 ? "" : "s";
        tvOrder.setText(getContext().getString(R.string.order_summary_payment_format, count, plural));

        boolean isServiceChangeEnabled = isServiceChangeEnabled();
        getProcessor().setServiceChargePercentage(isServiceChangeEnabled ?
                place.getServiceChargePercentage() :
                BigDecimal.ZERO);

        recalculate(processor);

        boolean pickUpSupported = !Boolean.FALSE.equals(place.getIsPickupEnabled());
        boolean deliverAtTableSupported = place.isTableDeliveryEnabled();
        if (pickUpSupported) {
            switchAtBar.setChecked(true);
        } else if (deliverAtTableSupported) { // only table supported
            switchAtTable.setChecked(true);
        }
        setVisibility(containerTable, deliverAtTableSupported && !pickUpSupported);

        switchAtTable.setOnCheckedChangeListener(
                (compoundButton, checked) -> {
                    setVisibility(containerTable, checked);
                });

        switchAtTable.setEnabled(deliverAtTableSupported);
        switchAtBar.setEnabled(pickUpSupported);
    }

    private ViewModelAdapter getAdapter(List<CreditCardInfo> cards) {

        View.OnClickListener onClickListener = view -> {
            CreditCardInfo checkedItem = (CreditCardInfo) view.getTag();
            boolean checked = checkedItem.isChecked();
            clearAllCheckedCards();
            checkedItem.setChecked(checked);
            enableCreditDetails(!checked);
            adapter.notifyDataSetChanged();
        };

        ViewModelAdapter adapter = new ViewModelAdapter(getContext(), cards, null);
        ViewModelHolderFactory factory = new ViewModelHolderFactory() {
            {
                add(CreditCardInfo.class, CreditCardUsageHolder.getLayout(), view ->
                        new CreditCardUsageHolder(view, onClickListener));
            }
        };

        adapter.setFactory(factory);
        return adapter;
    }

    protected void clearAllCheckedCards() {
        for (Object item : adapter.getDataItems()) {
            ((CreditCardInfo) item).setChecked(false);
        }
    }

    private void recalculate(IOrderProcessor processor) {
        BigDecimal discount = processor.getDiscountValue();
        tvDiscountAmount.setText(notZero(discount) ? getDiscountAmount(discount) : "");
        setVisibility(tvDiscountAmountAed, notZero(discount));
        BigDecimal tip = processor.getTipValue();
        tvTipAmount.setText(notZero(tip) ? getContext().getString(R.string.order_amount_format, tip) : "");
        setVisibility(tvTipAmountAed, notZero(tip));
        BigDecimal vip = processor.getVipCharge();
        tvVipAmount.setText(notZero(vip) ? getContext().getString(R.string.order_amount_format, vip) : "");
        setVisibility(tvVipAmountAed, notZero(vip));
        BigDecimal serviceCharge = processor.getServiceChargeAbsolute();
        tvServiceChargeAmount.setText(getContext().getString(R.string.order_amount_format, serviceCharge));
        tvAmountTotal.setText(getContext().getString(R.string.order_amount_format, processor.getTotal()));
    }

    @NotNull
    private String getDiscountAmount(BigDecimal discount) {
        return "-" + getContext().getString(R.string.order_amount_format, discount);
    }

    @OnClick(R.id.spinner_discount)
    public void selectDiscount() {
        getAnalytics().selectDiscount();
        selectOption(getString(R.string.dialog_title_select_discount),
                null,
                place.getDiscounts(),
                spinnerDiscount,
                R.string.select_discount,
                discount -> {
                    BigDecimal currentDiscount = getProcessor().getDiscountValue();
                    getProcessor().setDiscount(discount);
                    recalculate(getProcessor());
                    if (!MoneyUtils.greaterThanZero(currentDiscount) && discount != null && MoneyUtils.greaterThanZero(discount.getPercentage())) {
                        showDiscountInfo();
                    }
                }, new Discount() {
                    @Override
                    public String getVisualName() {
                        return super.getName();
                    }
                });
    }

    private void showDiscountInfo() {
        dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.dialog_title_discount_info), getString(R.string.dialog_message_select_discount));
    }

    @OnClick(R.id.spinner_tip)
    public void selectTip() {

        selectOption(getString(R.string.dialog_title_select_tip),
                getString(R.string.tip_message),
                TIPS,
                spinnerTip,
                R.string.select_tip,
                tip -> {
                    getProcessor().setTip(tip);
                    recalculate(getProcessor());
                }, new Tip("", 0, new BigDecimal(0)));
    }

    @OnClick(R.id.btn_vip_info)
    public void vipInfo() {
        dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.vip_info_title), getString(R.string.vip_info_text));
    }

    @OnClick(R.id.btn_saved_card_info)
    public void savedCardInfo() {
        dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.saved_card_info_title), getString(R.string.saved_card_info_text));
    }

    @Override
    public void onDestroyView() {
        placeHeaderHolder.unBind();
        super.onDestroyView();
    }

    @OnClick(R.id.spinner_table)
    void selectTable() {
        Logger.d(TAG, "Select table...");
        selectOption(getString(R.string.dialog_title_select_table),
                getString(R.string.payment_ask_table_number),
                place.getTables(),
                spinnerTable,
                R.string.choose_table, table -> {
                },
                null,
                () -> {
                });
    }

    @OnClick(R.id.button_pay)
    void pay() {

//        AuthToken authToken = getPreferencesStorage().getAuthToken();
//        if (authToken.isGuest() && !authToken.isAcceptedTerms()) {
//            dialog = DialogManager.showAgreeDialog(getActivity(),
//                    getString(R.string.terms_and_conditions_title),
//                    getText(R.string.terms_and_conditions_message).toString(),
//                    getString(R.string.terms_and_condition_agree),
//                    () -> getPreferencesStorage().getAuthToken().setAcceptedTerms(true));
//            return;
//        }

        getAnalytics().purchase();

        CreditCardInfo selectedSavedCard = getSelectedSavedCard();
        boolean saveCard = shouldSaveCard();
        if (selectedSavedCard == null && !cardPaymentFragment.isValid()) {
            getAnalytics().invalidPaymentDetails();
            return;
        }

        if (!billingAddressHolder.isValidEmail()) {
            getAnalytics().invalidEmail();
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_populate_billing_email),
                    getString(R.string.error_populate_billing_email));
            return;
        }
        if (!billingAddressHolder.isBillingValid()) {
            getAnalytics().invalidBilling();
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_populate_billing),
                    getString(R.string.error_populate_billing_details));
            return;
        }

        if (getProcessor().getNumberOfActiveOrders() >= 2) {
            getAnalytics().already2Orders();
            dialog = DialogManager.showInfoDialog(getActivity(),
                    getString(R.string.error_too_may_active_orders_title),
                    getString(R.string.error_too_may_active_orders_message));
            return;
        }

        if (progressBar.isWorking()) {
            return;
        }

        if (!isWorking()) {
            showToast(getString(R.string.bar_is_closed));
            return;
        }

        String message = getValidationError();
        if (message != null) {
            dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.error_correct_input), message);
            return;
        }

        saveBillingInfo();

        enableFields(false);
        Logger.d(TAG, "Proceed with paying...");
        // manually increment progress bar, that will be decremented once payment finishes
        showProgressBar(true);
        ActionCallback<Order> callback = new ActionCallback<Order>(progressBar, getActivity()) {

            @Override
            public void onSuccess(Order order) {
                Logger.d(TAG, "DL Order created: " + delayedResponse);
                if (delayedResponse) {
                    Logger.d(TAG, "DL order created, but onPaused, so ignore created order");
                    showProgressBar(false);
                    enableFields(true);
                    return;
                }
                orderId = order.getId();
                storeOrder(order);
                cardPaymentFragment.pay(
                        order.getPaymentAuthorizationLink(),
                        order.getPaymentOrderCode(),
                        toSavedCardPayment(order));
            }

            @Override
            public void onError(int code, String message, String errorBody) {
                showErrorBody(errorBody);
                Logger.d(TAG, "DL Order creation error");
                showProgressBar(false);
                showToast(message);
                enableFields(true);
            }

            private void showErrorBody(String errorBody) {
                if (!TextUtils.isEmpty(errorBody)) {
                    dialog = DialogManager.showOkDialog(getActivity(),
                            getString(R.string.error_an_error), errorBody);
                }
            }
        };

        Integer barId = null; //switchAtBar.isChecked() ? place.getBars().get(0).getId() : null;
        Integer tableId = switchAtTable.isChecked() ? getSelectedTable() : null;
        OrderRequest order = getProcessor().asOrderRequest(barId, tableId);
        order.setCardInfo(selectedSavedCard);
        order.setSaveCardInfo(selectedSavedCard == null && saveCard);
        order.setBillingAddress(getCreditCardInfo().setCardBillingAddress(new BillingAddress()));
        Logger.i(TAG, "post order:" + getGson().toJson(order));
        getApiCalls().postOrder(order).enqueue(trackCallback(callback));
    }

    public void saveBillingInfo() {
        if (cbSaveBilling.isChecked()) {
            SettingsPreferences prefs = getPreferencesStorage().getSettingsPreferences();
            //null indicate not to overwrite first and last name
            billingAddressHolder.setCardBillingAddress(prefs, null);
            getPreferencesStorage().save(prefs);
        }
    }

    private SavedCardPayment toSavedCardPayment(Order order) {
        CreditCardInfo card = getSelectedSavedCard();

        billingAddressHolder.captureValues();
        String email = settingsPreferences.getEmail();

        SavedCardPayment savedCardPayment = card == null ?
                new SavedCardPayment() :
                new SavedCardPayment(
                        card.getScheme(),
                        card.getCardToken(),
                        card.getCardholderName(),
                        card.getMaskedPan(),
                        !TextUtils.isEmpty(email) ? email : card.getEmail(),
                        card.getFirstName(),
                        !TextUtils.isEmpty(card.getLastName()) ? card.getLastName() : "",
                        card.getExpiry(),
                        order.getCurrency(),
                        (int) (order.getFinalPrice() * 100),
                        order.getId());

        return savedCardPayment;
    }

    private String getValidationError() {
        return switchAtTable.isChecked() && getSelectedTable() == null ?
                getString(R.string.error_table_not_selected) :
                null;
    }

    private CreditCardInfo getSelectedSavedCard() {
        return (CreditCardInfo)ListUtil.findFirst(adapter.getDataItems(), item -> ((CreditCardInfo)item).isChecked());
    }

    private void enableFields(boolean enabled) {
//        enableCreditDetails(enabled);
//
//        etEmail.setEnabled(enabled);
//        spinnerTip.setEnabled(enabled);
//        spinnerDiscount.setEnabled(enabled);
//        spinnerTable.setEnabled(enabled);

    }

    private void enableCreditDetails(boolean enabled) {
//        etCardHolder.setEnabled(enabled);
//        etValidUntil.setEnabled(enabled);
//        etCardNo.setEnabled(enabled);
//        etCcvCode.setEnabled(enabled);
//        saveCardCheckBox.setEnabled(enabled);

        setVisibility(saveCardCheckBox, enabled);
        setVisibility(savedCardInfoContainer, enabled);
        setVisibility(cardFragmentContainer, enabled);
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
            processNetworkInternationalResult(paymentData);
        }
        else {
            showProgressBar(false);
            Logger.i(TAG, "Payment canceled");
//            Activity.RESULT_CANCELED -> onCardPaymentCancelled();
            Toast.makeText(getContext(), "Payment canceled", Toast.LENGTH_LONG).show();
        }
    }

    private void processNetworkInternationalResult(final CardPaymentData paymentData) {
        if (!isResumed) {
            Logger.w(TAG, "not resumed on payment result");
            executeWhenResume(paymentData);
            return;
        }
        Logger.w(TAG, "resumed on payment result");
        processPaymentResult(paymentData);
    }

    private void executeWhenResume(CardPaymentData paymentData) {
        trackCallback(new ActionCallback<Void>(ProgressBarCounter.NO_PROGRESS_BAR, null) {
            {
                // mark es executed until onPause, so that it executes delayed on onResume
                delayedResponse = true;
            }
            @Override
            public void onSuccess(Void body) {
                Logger.i(TAG, "onResume execute processPaymentResult");
                processPaymentResult(paymentData);
            }

            @Override
            protected void onError(int code, String message, String errorBody) {
                Logger.i(TAG, "onResume execute processPaymentResult error");
            }
        });
    }

    private void processPaymentResult(CardPaymentData paymentData) {
        int code = paymentData.getCode();
        if (code == CardPaymentData.STATUS_PAYMENT_AUTHORIZED ||
                code == CardPaymentData.STATUS_PAYMENT_CAPTURED) {
            Logger.i(TAG, "process payment result : on authorized");
            onCardAuthorized();
            getAnalytics().payed();
        } else {
//                code == CardPaymentData.STATUS_PAYMENT_FAILED
//                code == CardPaymentData.STATUS_GENERIC_ERROR
            Logger.i(TAG, "process payment result : error");
            showProgressBar(false);
            enableFields(true);
            repopulateOrder();
            getAnalytics().paymentFailed();
            dialog = DialogManager.showInfoDialog(getActivity(), getString(R.string.payment_error_title), paymentData.getReason());
        }
    }

    private void repopulateOrder() {
        OrderPreparation repeatOrder = getProcessor().getMatchingOrderPreparation(orderId);
        getProcessor().merge(repeatOrder);
        getProcessor().setDiscount(repeatOrder.getDiscount());
        getProcessor().setTip(repeatOrder.getTip());
        getProcessor().setVipCharge(repeatOrder.getVipCharge());
        getProcessor().setServiceChargePercentage(repeatOrder.getServiceCharge());
    }

    private boolean isWorking() {
        OpenHoursData openHours = TimeUtils.getOpenHours(place.getWorkHours(), place.getTimeZoneId());
        return !Boolean.FALSE.equals(openHours.isOpen());
    }

    private Integer getSelectedTable() {
        Table table = (Table)spinnerTable.getTag();
        return table != null ? table.getId() : null;
    }

    private void storeOrder(Order order) {
        getProcessor().storeOrder(order);
        getProcessor().next(shouldSaveCard() ? getCreditCardInfo() : null);
        getProcessor().save();
    }

    private CreditCardInfo getCreditCardInfo() {
        CreditCardInfo selectedSavedCard = getSelectedSavedCard();
        String cardholderName = selectedSavedCard != null ?
                selectedSavedCard.getCardholderName() :
                cardPaymentFragment.getCardholderName();
        return billingAddressHolder.setCardBillingAddress(new CreditCardInfo(), cardholderName);
    }

    private boolean shouldSaveCard() {
        return saveCardCheckBox.isChecked();
    }

    private void showStatus(int orderId) {
        Intent intent = OrderStatusActivity.getOrderPreviewActivity(getContext(), orderId, false);
        getContext().startActivity(intent);
    }

    @Override
    public void init(Bundle bundle) {
        super.init(bundle);
        String placeString = bundle.getString(ExtrasKey.PLACE_EXTRA);
        place = getGson().fromJson(placeString, Place.class);
        getProcessor().forPlace(place);
    }

    @Override
    public boolean onBackPress() {
        return closeDialog();
    }

    private void onCardAuthorized() {
        if (orderId == -1) {
            throw new RuntimeException();
        }
        OrderPreparation preparation = getProcessor().getMatchingOrderPreparation(orderId);
        preparation.setPaymentSuccess(true);
        preparation.getOrder().captureLastModified();
        getProcessor().save();
        checkSavedCards(orderId);
        showStatus(orderId);
    }

    private void checkSavedCards(int checkOrderId) {
        if (!shouldSaveCard()) {
            Logger.i(TAG, "not saving card");
            return;
        }
        final CreditCardInfo newCreditCard = getCreditCardInfo();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Logger.i(TAG, "getSavedCards, is resumed:" + isResumed);
            String username = getPreferencesStorage().getAuthToken().getUrlUsername();
            getApiCalls().getSavedCreditCards().enqueue(new ActionCallback<List<CreditCardInfo>>(ProgressBarCounter.NO_PROGRESS_BAR, null) {

                @Override
                public void onSuccess(List<CreditCardInfo> cards) {
                    SettingsPreferences settingsPreferences = getPreferencesStorage().getSettingsPreferences();
                    Logger.i(TAG, "Number of cards" + cards.size());
                    if (cards.isEmpty()) {
                        return;
                    }

                    Set<CreditCardInfo> cardsSet = new HashSet<>();
                    CreditCardInfo.mergeCards(cardsSet, settingsPreferences.getCards());
                    List<CreditCardInfo> newlyAdded = CreditCardInfo.mergeCards(cardsSet, cards);
                    for (CreditCardInfo newCard : newlyAdded) {
                        newCreditCard.setCardBillingAddress(newCard);
                    }

                    Logger.i(TAG, "Number of unique cards" + cardsSet.size());
                    settingsPreferences.setCards(new ArrayList<>(cardsSet));
                    getPreferencesStorage().save(settingsPreferences);
                    OrderPreparation matchingOrderPreparation = getProcessor().getMatchingOrderPreparation(checkOrderId);
                    matchingOrderPreparation.setSavedCard(null);
                    getProcessor().save();
                    getApiCalls().deleteSavedCards().enqueue(new ActionCallback<Void>(ProgressBarCounter.NO_PROGRESS_BAR, null) {
                        @Override
                        protected void onError(int code, String message, String errorBody) {
                        }
                    });
                }

                @Override
                protected void onError(int code, String message, String errorBody) {
                }
            });
        }, CHECK_SAVED_CARDS_DELAY);
    }

    public interface SelectionChanged<T extends NamedObject> {

        void onSelectionChanged(T newPosition);
    }
}
