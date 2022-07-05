/*
 * Copyright (c) All rights reserved DrinkLink
 */

package org.drinklink.app.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NamedObject extends IdObject {

    public NamedObject(int id, String name) {
        super(id);
        this.name = name;
    }

    public String name;

    public Image image;

    public Image getImage() {
        if (image == null) {
            image = new Image();
        }
        return image;
    }

    public String getVisualName() {
        return getName();
    }

    public String getSelectedName() {
        return getName();
    }

    public String getSeparator() {
        return ", ";
    }

    public String getVisualAddition() {
        return null;
    }

    public static <T extends NamedObject> String getPreview(List<T> items) {
        StringBuilder preview = new StringBuilder();
        boolean isFirst = true;
        for (T item: items) {
            if (isFirst) {
                isFirst = false;
            } else {
                preview.append(item.getSeparator());
            }
            preview.append(item.getVisualName());
        }
        return preview.toString();
    }

}
