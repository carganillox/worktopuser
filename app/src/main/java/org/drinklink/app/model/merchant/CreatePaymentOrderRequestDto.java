package org.drinklink.app.model.merchant;

import java.util.Map;

import lombok.Data;

@Data
public class CreatePaymentOrderRequestDto {

    private String action;
    private PaymentOrderAmountDto amount;
    private String language;
    private String description;
    private Map<String, String> merchantAttributes;
    private BuildingAddressDto billingAddress;
    private String emailAddress;
}