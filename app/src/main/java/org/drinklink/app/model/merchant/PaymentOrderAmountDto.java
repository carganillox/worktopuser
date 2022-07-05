package org.drinklink.app.model.merchant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderAmountDto {
    private Integer value;
    private String currencyCode;
}