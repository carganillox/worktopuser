package org.drinklink.app.model.merchant;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class PaymentLinks {
    @SerializedName("payment")
    private Href payment;
    @SerializedName("payment-authorization")
    private Href paymentAuthorization;
}