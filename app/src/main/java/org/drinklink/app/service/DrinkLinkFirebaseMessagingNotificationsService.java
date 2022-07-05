/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.drinklink.app.R;
import org.drinklink.app.api.ApiService;
import org.drinklink.app.dependency.ApplicationModule;
import org.drinklink.app.dependency.DependencyResolver;
import org.drinklink.app.exception.OrderNotFound;
import org.drinklink.app.loader.ActionCallback;
import org.drinklink.app.loader.ProgressBarCounter;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.Order;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.model.request.OrderResponse;
import org.drinklink.app.persistence.PreferencesStorage;
import org.drinklink.app.persistence.model.OrderPreparation;
import org.drinklink.app.persistence.model.SettingsPreferences;
import org.drinklink.app.ui.activities.OrderStatusActivity;
import org.drinklink.app.utils.Analytics;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */

public class DrinkLinkFirebaseMessagingNotificationsService extends FirebaseMessagingService {

    private static final String TAG = "DrinkLinkFirebaseMessagingNotificationsService";
    private static final String ORDER_KEY = "order";

    @Inject
    public IOrderProcessor orderProcessor;

    @Inject
    public PreferencesStorage preferencesStorage;

    @Inject
    @Named(ApplicationModule.API_SERVICE)
    public ApiService apiService;

    public Handler handler;

    private Analytics analytics;

    public DrinkLinkFirebaseMessagingNotificationsService() {
        DependencyResolver.getComponent().inject(this);
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Logger.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Logger.d(TAG, "Message data payload: " + remoteMessage.getData());
            String orderJson = remoteMessage.getData().get(ORDER_KEY);
            Logger.d(TAG, "Order json: " + orderJson);
            if (orderJson != null) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
                Order order = gson.fromJson(orderJson, Order.class);
                // Handle message within 10 seconds
                handleNow(order);
            }

            //Check if data needs to be processed by long running job
            // For long-running tasks (10 seconds or more) use WorkManager.
            //scheduleJob();
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Logger.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void log(RemoteMessage remoteMessage) {
        new Handler(Looper.getMainLooper()).postAtFrontOfQueue(() -> {
            String test = convertWithIteration(remoteMessage.getData());
            Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
        });
    }
    // [END receive_message]

    public String convertWithIteration(Map<String, ?> map) {
        StringBuilder mapAsString = new StringBuilder("{");
        for (String key : map.keySet()) {
            mapAsString.append(key + "=" + map.get(key) + ", ");
        }
        mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
        return mapAsString.toString();
    }


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Logger.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(final Order order) {
        Logger.d(TAG, "Short lived task is done: " + order);
        handler.post(() -> {
            try {
                alertOrNotify(order);
            } catch (NullPointerException | OrderNotFound orderNotFound) {
                if (order.isFinished()) {
                    Logger.d(TAG, "Order not found, but it is already finished, so it maybe deleted");
                    return;
                }
                Logger.d(TAG, "Order not found. Load order: " + order.getId() + ", order: " + order);
                apiService.getOrder(order.getId()).enqueue(new ActionCallback<OrderResponse>(ProgressBarCounter.NO_PROGRESS_BAR, null) {
                    @Override
                    public void onSuccess(OrderResponse order) {
                        try {
                            Logger.i(TAG, "Recover and alertOrNotify");
                            orderProcessor.recoverOrder(order);
                            alertOrNotify(order);
                        } catch (OrderNotFound orderNotFound) {
                            throw orderNotFound;
                        }
                    }

                    @Override
                    protected void onError(int code, String message, String errorBody) {
                        Logger.i(TAG, "get order error: " + code + ", " + message + ", " + errorBody);
                    }
                });
                //throw orderNotFound;
            }
        });
    }

    private void alertOrNotify(Order order) throws OrderNotFound {

        OrderPreparation matchingOrderPreparation = orderProcessor.getMatchingOrderPreparation(order.getId());
        if (matchingOrderPreparation == null) {
            Logger.i(TAG, "Order not found");
            throw new OrderNotFound(order.getId());
        } else {
            Logger.i(TAG, "Matching order preparation: " + matchingOrderPreparation + ", " + matchingOrderPreparation.getOrder().getCurrentOrderState());
        }

        handleSavedCard(order, matchingOrderPreparation);
        Logger.i(TAG, "saved cards handled finished");

        OrderStates previousOrderState = orderProcessor.updateCurrentOrder(order, matchingOrderPreparation);
        Logger.i(TAG, "previous order state: " + previousOrderState);
        // update order anyway regardless if Ready alert or not
        boolean isOrderUpdateHandled = orderProcessor.updateOrder(order, previousOrderState);
        Logger.i(TAG, "Order update handled: " + isOrderUpdateHandled);

        if (order.shouldAlertOrderReady(previousOrderState)) {
            Boolean alertHandledByActiveActivities = orderProcessor.alertOrder(order.getId(), order.isBarOrder());
            Logger.i(TAG, "order not alerted, start activity, on Android < Oreo: " + alertHandledByActiveActivities);
            if (!Boolean.TRUE.equals(alertHandledByActiveActivities)) {
                // this will work if :
                // a) alertHandledByActiveActivities == false => application is active but not activity that knows to handle, we can start activity
                // b) alertHandledByActiveActivities == null && Android version < O => application not active, but we can start activity
                // try to handle alarm by open activities, if not set notification with vibration, sound. light
                Logger.i(TAG, "start activity for Ready Order");
                Context context = getApplicationContext();
                Intent intent = OrderStatusActivity.getOrderPreviewFromNotificationActivity(context, order.getId(), true);
                context.startActivity(intent);
                // c) alertHandledByActiveActivities == null && Android version > O => we will handle this by Heads Up Notification
                if (alertHandledByActiveActivities == null) {
                    Logger.i(TAG, "Android version > O, handle by Heads Up Notification");
                    sendNotification(order, true);
                }
            } else {
                Logger.i(TAG, "alert handled by active activity");
            }
        } else {
            if (!isOrderUpdateHandled && order.shouldNotifyState(previousOrderState)) {
                Logger.i(TAG, "order changed notification");
                sendNotification(order, false);
                logAnalytics(order);
            } else {
                Logger.i(TAG, "notification handled by active activity: " + isOrderUpdateHandled + ", is needed: " + order.shouldNotifyState(previousOrderState));
            }
        }
    }

    private void logAnalytics(Order order) {
        if (analytics == null) {
            analytics = new Analytics(getApplicationContext(), TAG);
        } else if (OrderStates.Accepted.equals(order.getCurrentOrderState())) {
            analytics.accepted();
        } else if (OrderStates.Rejected.equals(order.getCurrentOrderState())) {
            analytics.rejected();
        } else if (OrderStates.Ready.equals(order.getCurrentOrderState())) {
            analytics.readyToCollect();
        } else if (OrderStates.Collected.equals(order.getCurrentOrderState())) {
            analytics.collected();
        }
    }

    private void handleSavedCard(Order order, OrderPreparation matchingOrderPreparation) {
        CreditCardInfo savedCard = matchingOrderPreparation.getSavedCard();
        if (savedCard != null && OrderStates.Accepted.equals(order.getCurrentOrderState())) {
            Logger.i(TAG, "received order with saved card");

            apiService.getSavedCreditCards().enqueue(new ActionCallback<List<CreditCardInfo>>(ProgressBarCounter.NO_PROGRESS_BAR, null) {

                @Override
                public void onSuccess(List<CreditCardInfo> cards) {
                    SettingsPreferences settingsPreferences = preferencesStorage.getSettingsPreferences();
                    Logger.i(TAG, "Number of cards" + cards.size());
                    if (!cards.isEmpty()) {
                        Set<CreditCardInfo> cardsSet = new HashSet<>();
                        CreditCardInfo.mergeCards(cardsSet, settingsPreferences.getCards());
                        List<CreditCardInfo> newlyAdded = CreditCardInfo.mergeCards(cardsSet, cards);
                        for (CreditCardInfo newCard : newlyAdded) {
                            savedCard.setCardBillingAddress(newCard);
                        }

                        Logger.i(TAG, "Number of unique cards" + cardsSet.size());
                        settingsPreferences.setCards(new ArrayList<>(cardsSet));
                        preferencesStorage.save(settingsPreferences);
                        apiService.deleteSavedCards().enqueue(new ActionCallback<Void>(ProgressBarCounter.NO_PROGRESS_BAR, null) {

                            @Override
                            protected void onError(int code, String message, String errorBody) {
                                Logger.i(TAG, "delete saved card failed" );
                            }
                        });
                    }
                }

                @Override
                protected void onError(int code, String message, String errorBody) {
                    Logger.i(TAG, "get saved card failed" );
                }

            });
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(Order order, boolean isRingAlert) {

        Logger.i(TAG, "send notification");

        boolean soundNotification = preferencesStorage.getSettingsPreferences().isStateChangeSoundOn();
        boolean soundAlert = preferencesStorage.getSettingsPreferences().isReadySoundOn();

        String channelId = getChannel(getApplicationContext(), isRingAlert, soundNotification, soundAlert);
        Notification notification = getNotification(getApplicationContext(), order, isRingAlert, soundNotification, soundAlert, channelId);

        Logger.i(TAG, "notification on channel: " + channelId);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(order.getId(), notification);
    }

    public static String getChannel(Context context, boolean isRingAlert, boolean soundNotification, boolean soundAlert) {
        String channelId;
        if (isRingAlert) {
            channelId = context.getString(soundAlert ? R.string.notifications_ring_channel_id : R.string.notifications_silent_channel_id);
        } else {
            channelId = context.getString(soundNotification ? R.string.notifications_channel_id : R.string.notifications_silent_channel_id);
        }
        return channelId;
    }

    public static Notification getNotification(Context context, Order order, boolean isRingAlert, boolean soundNotification, boolean soundAlert, String channelId) {
        Intent intent = OrderStatusActivity.getOrderPreviewFromNotificationActivity(context, order.getId(), false);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder(context, channelId, order, pendingIntent);

        if (isRingAlert) {
//          https://developer.android.com/training/notify-user/time-sensitive
            notificationBuilder = notificationBuilder
                    .addAction(R.drawable.ico_drink1, context.getString(R.string.dismiss), pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(pendingIntent, true);
            if (soundAlert) {
                Logger.i(TAG, "onReady notification");
                notificationBuilder = notificationBuilder.setSound(Settings.System.DEFAULT_RINGTONE_URI, AudioManager.STREAM_ALARM);
            } else {
                Logger.i(TAG, "onReady silent notification");
            }
        } else {
            if (soundNotification) {
                Logger.i(TAG, "not onReady notification");
                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder = notificationBuilder.setSound(defaultSoundUri);
            }  else {
                Logger.i(TAG, "not onReady silent notification");
            }
        }
        return notificationBuilder.build();
    }

    private static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, Order order, PendingIntent pendingIntent) {
        String code = order.getCode() != null ? " " + order.getCode() : "";
        String orderState = context.getString(order.getCurrentOrderState().getNotifyStringResId());
        String itemsPreview = context.getString(R.string.your_order_update_notification, orderState, order.getItemsPreview(), code);
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo_drinklink)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_drinklink))
                .setContentTitle(context.getString(R.string.your_order_update_title, orderState))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(itemsPreview))
                .setContentText(itemsPreview)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent);
    }
}