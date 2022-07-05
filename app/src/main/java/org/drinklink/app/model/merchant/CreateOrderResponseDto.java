package org.drinklink.app.model.merchant;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class CreateOrderResponseDto {
    @SerializedName("reference")
    private String reference;
    @SerializedName("_links")
    private PaymentLinks paymentLinks;
    @SerializedName("paymentMethods")
    private PaymentMethods paymentMethods;
}
