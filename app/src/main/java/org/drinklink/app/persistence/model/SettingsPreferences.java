/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.persistence.model;

import org.drinklink.app.model.BillingAddress;
import org.drinklink.app.model.CreditCardInfo;
import org.drinklink.app.model.PaymentOption;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 */

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class SettingsPreferences extends BillingAddress {

    private boolean readySoundOn;
    private boolean stateChangeSoundOn;
    private List<PaymentOption> paymentOptions;
    private List<CreditCardInfo> cards;

    public SettingsPreferences(String email, boolean readySoundOn, boolean stateChangeSoundOn) {
        setEmail(email);
        this.readySoundOn = readySoundOn;
        this.stateChangeSoundOn = stateChangeSoundOn;
    }

    public List<PaymentOption> getPaymentOptions() {
        if (paymentOptions == null) {
            paymentOptions = new ArrayList<>();
        }
        return paymentOptions;
    }

    public List<CreditCardInfo> getCards() {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        return cards;
    }
}
