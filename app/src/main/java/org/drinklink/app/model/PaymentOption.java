package org.drinklink.app.model;

import lombok.Data;

/**
 *
 */
@Data
public class PaymentOption {

    private String address = "Flat 1,Building 123, Road 2345";
    private String city = "Manama";
    private String state = "Manama";
    private String country = "BHR";
    private String postalCodeOrCountryPhoneCode = "00973";

    private String token;
    private String phoneNumber = "009733";
    private String email = "customer-email@example.com";
    private String password = "password";
}
