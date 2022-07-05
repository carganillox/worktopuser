package org.drinklink.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class DeliveryLocation extends NamedObject {

    public Place place;

    private String description;
}
