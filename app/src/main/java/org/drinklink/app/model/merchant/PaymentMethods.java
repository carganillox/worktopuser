package org.drinklink.app.model.merchant;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class PaymentMethods {
    @SerializedName("card")
    List<String> card;
}