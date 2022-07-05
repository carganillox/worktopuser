package org.drinklink.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Bartender extends ApplicationUser {
    public int placeId;

    public Place place;
}
