package org.drinklink.app.model;

import org.drinklink.app.R;

import lombok.Getter;

@Getter
public enum OrderStates {

    //BE values
//    OrderCreated = 0,
//    Pending = 1,
//    Accepted = 2,
//    PaymentProcessed = 3,
//    Ready = 4,
//    Collected = 5,
//    Failed = 101,
//    Canceled = 102,
//    Rejected = 103,
//    NotCollected = 104,
//    PaymentFailed = 105

    OrderCreated(0, R.string.order_state_on_hold, R.color.manatee),

    Pending(1, R.string.order_state_on_hold, R.color.manatee),

    Accepted(2, R.string.order_state_preparing, R.color.manatee, R.string.order_state_notif_preparing, R.string.order_state_preparing),

    Processed(3, R.string.order_state_preparing, R.color.manatee, R.string.order_state_notif_preparing, R.string.order_state_preparing),

    Ready(4, R.string.order_state_ready, R.color.yellowgreen),

    Collected(5, R.string.order_state_collected, R.color.yellowgreen, R.string.order_state_collected, R.string.order_state_delivered),

    ReadyExpired(6, R.string.order_state_completed, R.color.yellowgreen),

    // NOT A REAL STATE IT IS JUST A SEPARATOR BETWEEN SUCCESSFUL-PENDING AND FAILED STATES
    FailedSeparator(100, R.string.order_state_failed, R.color.ruddy),

    Canceled(102, R.string.order_state_canceled, R.color.ruddy),

    Failed(101, R.string.order_state_failed, R.color.ruddy),

    Rejected(103, R.string.order_state_rejected, R.color.gold),

    NotCollected(104, R.string.order_state_not_collected, R.color.ruddy),

    PaymentFailed(105, R.string.order_state_payment_failed, R.color.gold);

    private int id;
    private int stringResId;
    private int tableStringResId;
    private int notifyStringResId;
    private int colorResId;

    OrderStates(int id, int stringResId, int colorResId) {
        this(id, stringResId, colorResId, stringResId, stringResId);
    }

    OrderStates(int id, int stringResId, int colorResId, int notifyStringResId, int tableStringResId) {
        this.id = id;
        this.stringResId = stringResId;
        this.colorResId = colorResId;
        this.notifyStringResId = notifyStringResId;
        this.tableStringResId = tableStringResId;
    }

    public int getId() {
        return id;
    }

    public int getStringResId(boolean isBarOrder) {
        return isBarOrder ? stringResId : tableStringResId;
    }

    public static OrderStates getOrderStateById(int id) {
        for (OrderStates state : OrderStates.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        return OrderCreated;
    }
}
