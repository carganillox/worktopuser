/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import org.drinklink.app.persistence.model.Internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class BillingAddress extends NamedObject {

    private String email;

    private String firstName;

    private String lastName;

    private String address;

    @Internal
    private String fullAddress;

    private String city;

    private String countryCode;

    @Internal
    private boolean billToCardHolder;

    public BillingAddress setCardBillingAddress(BillingAddress newBillingAddress) {
        newBillingAddress.setFirstName(this.firstName);
        newBillingAddress.setLastName(this.lastName);
        newBillingAddress.setEmail(this.email);
        newBillingAddress.setAddress(this.address);
        newBillingAddress.setCity(this.city);
        newBillingAddress.setCountryCode(this.countryCode);
        newBillingAddress.setFullAddress(this.fullAddress);
        newBillingAddress.setBillToCardHolder(this.isBillToCardHolder());
        return newBillingAddress;
    }
}
