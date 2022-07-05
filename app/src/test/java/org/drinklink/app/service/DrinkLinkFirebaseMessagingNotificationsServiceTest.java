package org.drinklink.app.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.drinklink.app.model.Order;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class DrinkLinkFirebaseMessagingNotificationsServiceTest {

    @Test
    public void testDeserialization() {
        String orderJson = "{\"Id\":2030,\"BarId\":1,\"Bartender\":\"ZinkBarmen1\",\"TableId\":null,\"CurrentState\":5,\"FacilityId\":1,\"Location\":\"Bar\",\"Code\":\"T33\",\"AdditionalInfo\":null,\"TimeToCollect\":\"2020-01-02T22:27:08.1859585Z\",\"OrderNumber\":5,\"OrderIdentificator\":\"T33\"}";

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        Order order = gson.fromJson(orderJson, Order.class);

        assertEquals(2030, order.getId());
    }

}