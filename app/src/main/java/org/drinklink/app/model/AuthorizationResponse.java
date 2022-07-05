package org.drinklink.app.model;

import lombok.Data;

/**
 *
 */
@Data
public class AuthorizationResponse implements IAuthorizationResponse {

    private String paymentOrderCode;

    private String paymentAuthorizationLink;
}
