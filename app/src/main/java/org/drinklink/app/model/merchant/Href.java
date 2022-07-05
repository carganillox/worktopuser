package org.drinklink.app.model.merchant;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Href {
    @SerializedName("href")
    private String href;
}