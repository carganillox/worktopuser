package org.drinklink.app.alarm;

import android.content.Context;
import android.content.Intent;

import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.ui.activities.OrderStatusActivity;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

/**
 *
 */
//@Singleton
public class OrderNotificationsManager {

    private OrderNotificationsManager() {
    }

    private static final String TAG = "NotificationManager";

//    public Context context;
//    public IOrderProcessor orderProcessor;
//    public PreferencesStorage preferencesStorage;
//
//    @Inject
//    public OrderNotificationsManager(Context context, IOrderProcessor orderProcessor, PreferencesStorage preferencesStorage) {
//        this.context = context;
//        this.orderProcessor = orderProcessor;
//        this.preferencesStorage = preferencesStorage;
//    }

//    public void init() {
//        orderProcessor.addOrderUpdateListener(new IOrderProcessor.OrderUpdateListenerAdapter() {
//            @Override
//            public boolean onOrderUpdated(Order order, OrderStates previousState) {
//                if (order.getCurrentOrderState() == previousState) {
//                    return false;
//                }
//
//                if (OrderStates.Ready.equals(order.getCurrentOrderState()) &&
//                        preferencesStorage.getSettingsPreferences().isReadySoundOn()) {
//                    setAlarm(order);
//                } else if (order.shouldNotifyState() &&
//                        preferencesStorage.getSettingsPreferences().isStateChangeSoundOn()) {
//                    playNotificationSound();
//                }
//                if (order.isFinished()) {
//                    removeAlarm(order);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onOrderAlert(int orderId) {
//                Logger.i(TAG, "onOrderAlert:" + orderId);
//                return false;
//            }
//        });
//    }

//    private void setAlarm(Order order) {
//        setAlarm(order.getId(), order.getCollectAtUtcMillis());
//    }
//
//    public void setAlarm(int requestCode, long collectAtUtcMillis) {
////        long notifyAt = collectAtUtcMillis - TimeUnit.MINUTES.toMillis(NOTIFY_MINUTES_BEFORE);
//        long notifyAt = System.currentTimeMillis();
//        // this could be implemented by directly notifying, but as there is already a mechanism through alarm, reusing it.
//        notifyReady(requestCode, notifyAt);
//    }
//
//    private void notifyReady(int requestCode, long notifyAt) {
//        Intent intent = new Intent(context, AlarmReceivers.class);
//        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, requestCode);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        Logger.i(TAG, "set alarm:" + requestCode + ",  " +
//                TimeUnit.MILLISECONDS.toSeconds(notifyAt - System.currentTimeMillis()));
//        alarmManager.set(AlarmManager.RTC_WAKEUP, notifyAt, pendingIntent);
//    }

//    public void removeAlarm(Order order) {
//        Intent intent = new Intent(context, AlarmReceivers.class);
//        int requestCode = order.getId();
//        intent.putExtra(ExtrasKey.ORDER_ID_EXTRA, requestCode);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        Logger.i(TAG, "remove alarm " + requestCode);
//        alarmManager.cancel(pendingIntent);
//    }

//    private void playNotificationSound() {
//        Logger.i(TAG, "play notifications sound");
//        try {
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(context, notification);
//            r.play();
//        } catch (Exception e) {
//            Logger.e(TAG, e.getMessage(), e);
//        }
//    }

    /**
     * @return true => alarm is handled, false => alarm is not handled
     */
    public static boolean alertOrderReady(Context context, IOrderProcessor orderProcessor, int orderId) {
        OrderPreparation matchingOrderPreparation = orderProcessor.getMatchingOrderPreparation(orderId);
        if (matchingOrderPreparation.getOrder().isFinished()) {
            Logger.i(TAG, "order finished skip alarm and notifications");
            return true;
        }

        Boolean alertHandledByActiveActivities = orderProcessor.alertOrder(orderId, matchingOrderPreparation.getOrder().isBarOrder());
        Logger.i(TAG, "order not alerted, start activity, on Android < Oreo: " + alertHandledByActiveActivities);
        if (!Boolean.TRUE.equals(alertHandledByActiveActivities)) {
            // this will work if :
            // a) alertHandledByActiveActivities == false => application is active but not activity that knows to handle, we can start activity
            // b) alertHandledByActiveActivities == null && Android version < O => application not active, but we can start activity
            Intent intent = OrderStatusActivity.getOrderPreviewActivity(context, orderId, true);
            context.startActivity(intent);
        }
        // c) alertHandledByActiveActivities == null && Android version > O => we will handle this by Heads Up Notification
        return alertHandledByActiveActivities != null;
    }
}
