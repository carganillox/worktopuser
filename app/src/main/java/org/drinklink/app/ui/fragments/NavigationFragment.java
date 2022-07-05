/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.drinklink.app.R;
import org.drinklink.app.common.activity.ToolbarActivity;
import org.drinklink.app.common.constants.ExtrasKey;
import org.drinklink.app.common.fragment.OrderUpdateListenerFragment;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.patch.AppCleaner;
import org.drinklink.app.persistence.AuthToken;
import org.drinklink.app.ui.activities.MainActivity;
import org.drinklink.app.ui.activities.OrderHistoryActivity;
import org.drinklink.app.ui.activities.SettingsActivity;
import org.drinklink.app.ui.activities.SignInActivity;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.ui.viewholder.NavigationOrderHolder;
import org.drinklink.app.utils.IntentUtils;
import org.drinklink.app.utils.ListUtil;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessorPreview;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */

public class NavigationFragment extends OrderUpdateListenerFragment {

    private static final String TAG = "NavigationFragment";

    @BindView(R.id.order)
    View orderView;

    @BindView(R.id.order2)
    View orderView2;

    @BindView(R.id.lbl_menu_settings)
    TextView lblSettings;

    @BindView(R.id.lbl_menu_sign_in)
    TextView tvSignIn;

    @BindView(R.id.most_recent_orders)
    View mostRecentLabel;

    private boolean isLoggedIn;

    private NavigationOrderHolder order1Holder;
    private NavigationOrderHolder order2Holder;

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_navigation;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Runnable closeDrawer = () -> closeDrawer();
        order1Holder = new NavigationOrderHolder(orderView, closeDrawer);
        order2Holder = new NavigationOrderHolder(orderView2, closeDrawer);

        showVersion();
    }

    private void bindPendingOrders() {
        List<IOrderProcessorPreview> pendingOrders = getPendingOrders();
        bindOrder(pendingOrders, 0, order1Holder);
        bindOrder(pendingOrders, 1, order2Holder);
        setVisibility(mostRecentLabel, !pendingOrders.isEmpty());
    }

    private List<IOrderProcessorPreview> getPendingOrders() {
        return ListUtil.select(getProcessor().getOrderPreviews(), item -> {
                OrderStates state = item.getOrder().getCurrentOrderState();
                return state != OrderStates.Rejected &&
                        state != OrderStates.Canceled &&
                        state != OrderStates.Failed &&
                        !(state == OrderStates.OrderCreated && !item.isPaymentSuccess());
            });
    }

    private void setUserLoginInfo() {
        this.isLoggedIn = getPreferencesStorage().getAuthToken().isSignedIn();
        tvSignIn.setText(getString(isLoggedIn ? R.string.menu_sign_out : R.string.menu_sign_in));
    }

    private void showVersion() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            int version = pInfo.versionCode;
            lblSettings.setText(getString(R.string.menu_settings_format, version));
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage(), e);
        }
    }

    private void bindOrder(List<IOrderProcessorPreview> pendingOrders, int i, NavigationOrderHolder navigationOrderHolder) {
        boolean hasOrder = pendingOrders.size() > i;
        navigationOrderHolder.setViewVisibility(hasOrder);
        if (hasOrder) {
            IOrderProcessorPreview orderProcessor = pendingOrders.get(i);
            navigationOrderHolder.bind(getContext(), orderProcessor);
        }
    }

    protected void closeDrawer() {
        //TODO: find more elegant solution
        ((ToolbarActivity)getActivity()).closeDrawer();
    }

    @OnClick(R.id.menu_home)
    public void onHome() {
        getProcessor().reset();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(IntentUtils.CLEAR_AND_NEW);
        intent.putExtra(ExtrasKey.SHOW_STATUS, false);
        startActivity(intent);
    }

    @OnClick(R.id.menu_settings)
    public void onSettings() {
        startOverExistingActivity(SettingsActivity.class);
    }

    @OnClick(R.id.menu_sign_in)
    public void onSignIn() {
        if (isLoggedIn) {
            signOut();
        } else {
            startOverExistingActivity(SignInActivity.class);
        }
    }

    private void signOut() {
        dialog = DialogManager.showYesNoDialogEqual(getActivity(),
                getString(R.string.dialog_header_sign_out),
                getString(R.string.dialog_description_sign_out),
                () -> {
                    getPreferencesStorage().save(new AuthToken());
                    setUserLoginInfo();
                    closeDrawer();
                    clearData();
                    onHome();
                }, () -> {
                });
    }

    private void clearData() {
        Logger.i(TAG, "clear data start");
        getDataStorage().clear();
        getPreferencesStorage().clear();
        Logger.i(TAG, "clear data end");
    }

    @OnClick(R.id.menu_your_orders)
    public void onYourOrders() {
        startOverExistingActivity(OrderHistoryActivity.class);
    }

    private void startOverExistingActivity(Class<? extends ToolbarActivity> cls) {
        Intent intent = new Intent(getContext(), cls);
        intent.setFlags(IntentUtils.OVER_EXISTING);
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        closeDrawer();
        super.startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUserLoginInfo();
        bindPendingOrders();
    }

    @Override
    public boolean onOrderUpdated(Order order, OrderStates previousState) {
        checkAndUpdateHolderIfNeeded(order, order1Holder);
        checkAndUpdateHolderIfNeeded(order, order2Holder);
        return false;
    }

    @Override
    public boolean onOrderAlert(int orderId, boolean isBarOrder) {
        // do not handle alerts
        return false;
    }

    private void checkAndUpdateHolderIfNeeded(Order order, NavigationOrderHolder holder) {
        IOrderProcessorPreview order1Item = holder.getItem();
        if (order1Item != null && order.getId() == order1Item.getOrder().getId()) {
            holder.reBind();
        }
    }

    @OnClick(R.id.terms_and_conditions)
    public void termsAndConditions() {
        String url = "https://drinklink.ae/wp-content/uploads/2019/10/Terms-of-Service-DrinkLink.pdf";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.upload_logs)
    public void sendLogcatMail() {
        uploadLogs(getActivity());
    }

    public static void uploadLogs(Context context) {
        String body = saveLogsToFile();

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        // Set type to "email"
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"drinklink.dev1@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
//        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Upload logs " + android.os.Build.VERSION.SDK_INT);
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private static String saveLogsToFile() {
        StringBuilder builder = new StringBuilder();
        List<String> logs = Logger.dumpLogs();
        for (String line : logs) {
            builder.append(line);
        }
        return builder.toString();
    }
}
