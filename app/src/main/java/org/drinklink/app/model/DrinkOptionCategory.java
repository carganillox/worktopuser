/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DrinkOptionCategory extends NamedObject {

    public DrinkOptionCategory(DrinkOptionCategory cat) {
        super(cat.getName(), cat.getImage());
        this.isMultipleSelectionAlowed = cat.isMultipleSelectionAlowed;
        this.mixers = cat.mixers;
    }

    private boolean isMultipleSelectionAlowed;

    private List<DrinkOption> mixers;

    @Builder.Default
    private transient List<DrinkOption> selectedMixers = new ArrayList<>();
}