/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import androidx.annotation.DrawableRes;

import org.drinklink.app.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import payment.sdk.android.core.CardType;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"isChecked"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditCardInfo extends BillingAddress {

//
//    {
//        "id": 1,
//            x"maskedPan": "411111******1111",
//            x"expiry": "2021-11",
//            "cardholderName": "TEST CUSTOMER",
//            "scheme": "VISA",
//            "cardToken": "dG9rZW5pemVkUGFuLy92MS8vU0hPV19OT05FLy8xMTExMTExNDExMTExMTEx"
//    }


    private CreditCardType cardType;

    private String scheme;

    private String cardToken;

    private String cardholderName;

    private String maskedPan;

    private String expiry;

    private String CCV;

    private boolean isChecked;

    public static CreditCardType getCard(CardType cardType) {
        switch (cardType) {
            case MasterCard:
                return CreditCardType.MasterCard;
            case AmericanExpress:
                return CreditCardType.AmericanExpress;
            case JCB:
                return CreditCardType.JCB;
            case DinersClubInternational:
                return CreditCardType.DinersClubInternational;
            case Discover:
                return CreditCardType.Discover;
            default:
                return CreditCardType.Visa;
        }
    }
    @NotNull
    public static List<CreditCardInfo> mergeCards(Set<CreditCardInfo> cardsSet, List<CreditCardInfo> cards) {

        Collections.sort(cards, (c1, c2) -> c1.getId() - c2.getId());
        Set<CreditCardInfo> unique = new HashSet<>();
        unique.addAll(cards);
        List<CreditCardInfo> newlyAdded = new ArrayList<>();
        for (CreditCardInfo card : unique) {
            if (!cardsSet.contains(card)) {
                newlyAdded.add(card);
            }
        }
        cardsSet.addAll(unique);
        return newlyAdded;
    }

    @DrawableRes
    public int getLogo() {
        switch (scheme.toLowerCase()) {
            case "visa":
                return R.drawable.ic_logo_visa;
            case "mastercard":
                return R.drawable.ic_logo_mastercard;
            case "americanexpress":
                return R.drawable.ic_logo_amex;
            case "dinners":
                return R.drawable.ic_logo_dinners_clup;
            case "discover":
                return R.drawable.ic_logo_discover;
            case "jcb":
                return R.drawable.ic_logo_jcb;

        }
        return R.drawable.ic_logo_visa;
    }
}
