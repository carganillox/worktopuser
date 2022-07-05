package org.drinklink.app.common.fragment;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.drinklink.app.R;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.ui.dialog.DialogManager;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class OrderUpdateListenerFragment extends DrinkLinkFragment implements IOrderProcessor.OrderUpdateListener {

    private static final String TAG = "OrderUpdateListenerFragment";

    private static final long NOTIFICATION_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);

    public Ringtone player;

    @Override
    public boolean onOrderUpdated(Order order, OrderStates previousState) {
        if (order.shouldNotifyState(previousState) && !order.shouldAlertOrderReady(previousState)) {
            playMessageSound();
        }
        return false;
    }

    @Override
    public boolean isMatch(int orderId) {
        return true;
    }

    @Override
    public boolean onOrderAlert(int orderId, boolean isBarOrder) {
        playAlarm();
        Runnable dismiss = () -> {
            dismissAlarm();
        };
        dialog = DialogManager.showOkDialog(getActivity(), getString(R.string.alert_order_ready),
                getString(isBarOrder ?
                        R.string.alert_minute_left:
                        R.string.alert_table_order_ready), dismiss, dismiss);
        return true;
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
        dismissAlarm();
    }

    private void playAlarm() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        dismissAlarm();
        new Handler(Looper.getMainLooper()).postDelayed(() -> dismissAlarm(), NOTIFICATION_TIMEOUT_MS);
        if (preferencesStorage.getSettingsPreferences().isReadySoundOn()) {
            Logger.i(TAG, "play ringtone");
            player = RingtoneManager.getRingtone(getContext(), notification);
            player.play();
        } else {
            Logger.i(TAG, "play ringtone turned off");
        }
    }

    private void playMessageSound() {
        if (preferencesStorage.getSettingsPreferences().isStateChangeSoundOn()) {
            Logger.i(TAG, "play status change");
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), notification);
            ringtone.play();
        } else {
            Logger.i(TAG, "play status change turned off");
        }
    }

    private void dismissAlarm() {
        Ringtone currentPlayer = this.player;
        if (currentPlayer != null) {
            Logger.i(TAG, "dismiss player");
            currentPlayer.stop();
            this.player = null;
        }
    }
}
