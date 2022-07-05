/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.ui.viewmodel;

import android.content.Context;

import org.drinklink.app.R;
import org.drinklink.app.model.Bar;
import org.drinklink.app.model.OrderStates;
import org.drinklink.app.utils.Logger;
import org.drinklink.app.workflow.IOrderProcessorPreview;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 *
 */

@Data
public class StateItem {

    private static final String TAG = "StateItem";

    private static StateItem DEFAULT = new StateItem("DEFAULT", OrderStates.OrderCreated) {
        @Override
        public boolean isLastExecuted() {
            return true;
        }

        @Override
        public StateItem setState(OrderStates newState) {
            return null;
        }

        @Override
        public boolean isExecuted() {
            return true;
        }

        @Override
        protected void setAllPrevExecuted() {
        }

        @Override
        public void setBar(@NonNull Bar bar) {
        }

        @Override
        public void setRejected(String barmen, String rejectMessage, String additionalInfo) {
        }
    };

    protected StateItem next = DEFAULT;
    protected StateItem prev = DEFAULT;
    private OrderStateMatch stateMatch;
    private String barmen;
    private String rejectMessage;
    private String barDescription;
    private boolean hasDiscount = false;
    private boolean isTable;

    public StateItem(String name, OrderStates state, boolean isCancelable) {
        this(name, state);
        this.isCancelable = isCancelable;
    }

    public StateItem(String name, OrderStates... state) {
        this.name = name;
        this.stateMatch = new StateMatch(state);
    }

    public String getMessage() {
        return rejectMessage != null ? rejectMessage : "";
    }

    private String name;

    private boolean isExecuted;

    private boolean isCancelable;

    private boolean isRejected;

    public StateItem setNext(StateItem next) {
        this.next = next;
        next.setPrev(this);
        return next;
    }

    public boolean isLastExecuted() {
        return isExecuted && !next.isExecuted();
    }

    // return update StateItem
    public StateItem setState(OrderStates newState) {
        if (isStateMatch(newState)) {
            boolean prevIsExecuted = this.isExecuted;
            this.isExecuted = true;
            this.prev.setAllPrevExecuted();
            return !prevIsExecuted ? this : null;
        } else {
            return next.setState(newState);
        }
    }

    public boolean isStateMatch(OrderStates newState) {
        return this.stateMatch.isStateMatch(newState);
    }

    protected void setAllPrevExecuted() {
        Logger.d(TAG, "setAllPrevExecuted : " + getStateName());
        this.isExecuted = true;
        prev.setAllPrevExecuted();
    }

    @NotNull
    public String getStateName() {
        return stateMatch.toString();
    }

    public void setRejected(String barmen, String rejectMessage, String additionalInfo) {
        setRejectedFields(barmen, rejectMessage, additionalInfo);
        next.setRejected(barmen, rejectMessage, null);
    }

    protected void setRejectedFields(String barmen, String rejectMessage, String additionalInfo) {
        this.barmen = barmen;
        this.rejectMessage = rejectMessage != null ? rejectMessage : additionalInfo;
        this.isRejected = true;
        this.isExecuted = false;
    }

    public void setBar(@NonNull Bar bar) {
        this.barDescription = bar.getDescription();
        getNext().setBar(bar);
    }

    public StateItem get() {
        return this;
    }

    public static class DoubleState extends StateItem {

        private StateItem current;
        private final List<StateItem> items = new ArrayList<>();

        public DoubleState(StateItem... items) {
            super("", OrderStates.OrderCreated);
            this.items.clear();
            for (StateItem item: items) {
                this.items.add(item);
            }
            this.current = items[0];
        }

        @Override
        public StateItem get() {
            return current;
        }

        public StateItem setNext(StateItem next) {
            for (StateItem item: items) {
                item.setNext(next);
            }
            super.setNext(next);
            return next;
        }

        public void setPrev(StateItem prev) {
            for (StateItem item: items) {
                item.setPrev(prev);
            }
            super.setPrev(prev);
            this.prev = prev;
        }

        public StateItem setState(OrderStates newState) {
            for (StateItem item: items) {
                if (item.isStateMatch(newState)) {
                    current = item;
                    continue;
                }
            }
            StateItem stateItem = current.setState(newState);
            setExecuted(current.isExecuted);
            return stateItem;
        }

        protected void setAllPrevExecuted() {
            Logger.d(TAG, "setAllPrevExecuted : " + current.getStateName());
            for (StateItem item: items) {
                item.isExecuted = true;
            }
            super.setAllPrevExecuted();
        }

        @Override
        public void setRejected(String barmen, String rejectMessage, String additionalInfo) {
            for (StateItem item: items) {
                item.setRejectedFields(barmen, rejectMessage, additionalInfo);
            }
            setRejectedFields(barmen, rejectMessage, additionalInfo);
            next.setRejected(barmen, rejectMessage, null);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class PendingStateItem extends StateItem {

        private final IOrderProcessorPreview orderProcessor;
        private final Context context;

        public PendingStateItem(Context ctx, OrderStates state, boolean isCancelable, IOrderProcessorPreview orderProcessor) {
            super(null, state, isCancelable);
            this.orderProcessor = orderProcessor;
            this.context = ctx;
            this.setExecuted(true);
        }

        @Override
        public String getName() {
            return context.getString(R.string.state_item_on_hold_accepted);
//            return this.getNext() != null && getNext().isExecuted() ?
//                    context.getString(R.string.state_item_on_hold_accepted) :
//                    context.getString(R.string.state_item_on_hold_format, orderProcessor.getOrder().getOrderNumber());
        }
    }

    public interface OrderStateMatch {
        boolean isStateMatch(OrderStates state);
    }

    public class StateMatch implements OrderStateMatch {

        List<OrderStates> stateList;
        public StateMatch(OrderStates... states) {
            stateList = new ArrayList<>();
            for (OrderStates state: states) {
                stateList.add(state);
            }
        }

        @Override
        public boolean isStateMatch(OrderStates state) {
            return stateList.contains(state);
        }

        @Override
        public String toString() {
            return stateList.toString();
        }
    }
}
