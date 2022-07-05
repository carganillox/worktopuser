package org.drinklink.app.model;

/**
 *
 */
public interface IAuthorizationResponse {

    String getPaymentOrderCode();

    String getPaymentAuthorizationLink();
}
