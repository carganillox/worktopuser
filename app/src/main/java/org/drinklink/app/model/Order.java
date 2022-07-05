package org.drinklink.app.model;

import android.text.TextUtils;

import org.drinklink.app.model.request.OrderItemRequest;
import org.drinklink.app.persistence.model.Internal;
import org.drinklink.app.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Order extends OrderBase implements IAuthorizationResponse {

    public static long EXPIRED_TIME = TimeUnit.MINUTES.toMillis(10);

    private static String PAYMENT_CODE = "57c8b342e35e4261";
    private static String PAYMENT_AUTH_URL = "https://api-gateway-uat.ngenius-payments.com/transactions/paymentAuthorization";

    public String code;

    public Long collectAtUtcMillis;

    // Type of this field should be reconsidered
    public String bartender;

    public String customer;

    public Table table;

    public List<OrderState> orderStateHistory;

    public int currentState;

    private String reason;

    private Discount discount;

    private int orderNumber;

    private String additionalInfo;

    private String orderIdentificator; //"#9"

    private String timestamp;

    private String timeToCollect;

    private String orderReference;

    @Internal
    private transient long lastModifiedPrevious;
    @Internal
    private transient long lastModified;

    private String currency = "AED";

    private String paymentOrderCode;

    private String paymentAuthorizationLink;

    private Bar bar;

    public OrderStates getCurrentOrderStateForPreview() {
        OrderStates currentOrderState = getCurrentOrderState();
        if (currentOrderState == OrderStates.Ready && isExpired()) {
            currentOrderState = OrderStates.ReadyExpired;
        }
        return currentOrderState;
    }

    public OrderStates getCurrentOrderState() {
        return OrderStates.getOrderStateById(currentState);
    }

    public String getItemsPreview() {
        return NamedObject.getPreview(getItems());
    }

    public int getCount() {
        int sum = 0;
        for (OrderItemRequest item : getItems()) {
            sum += item.getQuantity();
        }
        return sum;
    }

    public long getCollectAtUtcMillis() {
        if (collectAtUtcMillis == null) {
            collectAtUtcMillis = TimeUtils.getDateTimeInMillisFromString(timeToCollect);
//            if (timeToCollect != null) {
//                collectAtUtcMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
//            } else {
//                collectAtUtcMillis = TimeUtils.getDateTimeInMillisFromString(timeToCollect);
//            }
        }
        return collectAtUtcMillis;
//        return System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
    }

    public long getRemainingTimeMs() {
        return getCollectAtUtcMillis() - TimeUtils.getCurrentTimeMs();
    }

    public boolean isExpired() {
        return isCollectTimeDefined() && getRemainingTimeMs() < -1 * EXPIRED_TIME;
    }

    public boolean isFinished() {
        OrderStates currentOrderState = getCurrentOrderState();
        return OrderStates.Collected.equals(currentOrderState) || isFailed();
    }

    public void captureLastModified() {
        lastModifiedPrevious = lastModified;
        lastModified = System.currentTimeMillis();
    }

    public boolean isUpdated() {
        return lastModifiedPrevious != lastModified;
    }

    public String getProductName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< getItems().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(getItems().get(i).getDrink().getName());
        }
        return sb.toString();
    }

    public boolean isCollectTimeDefined() {
        return getCollectAtUtcMillis() > 0;
    }

    public boolean isFailed() {
        return getCurrentOrderState().getId() > OrderStates.FailedSeparator.getId();
    }

    public boolean shouldNotifyState(OrderStates previousState) {
        OrderStates currentState = getCurrentOrderState();
        return !previousState.equals(currentState) &&
                currentState.getId() >= OrderStates.Accepted.getId()
                && !OrderStates.Collected.equals(currentState);
    }

    public boolean shouldAlertOrderReady(OrderStates previousOrderState) {
        return OrderStates.Ready.equals(getCurrentOrderState()) &&
                OrderStates.Ready.getId() > previousOrderState.getId();
    }

    public boolean isBarOrder() {
        return getTableId() == null;
    }

    public void update(Order currentOrder) {
        // Order updates doesn't provide all info, so some need to be reused from previous updates
        this.setItems(currentOrder.getItems());
        this.setFinalPrice(currentOrder.getFinalPrice());
        if (TextUtils.isEmpty(getOrderReference())) {
            this.setOrderReference(currentOrder.getOrderReference());
        }
    }
}