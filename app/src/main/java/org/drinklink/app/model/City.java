package org.drinklink.app.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class City extends NamedObject {

        public List<Area> areas;
}
