package org.drinklink.app.model;

import android.text.TextUtils;

import org.drinklink.app.dependency.ApplicationModule;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Drink extends NamedObject {

    // This is maybe not necessary since it can be also a part of the name.
    public String volume;

    public DrinkCategory category;

    public BigDecimal price = BigDecimal.ZERO;

    private String description;// = "Some description, long, longer, some description, long, longer";

    private String additionalInfo; // = "Additional info to be shown in dialog box";

    public List<DrinkOptionCategory> mixerCategories;

    private boolean allowIce;

    private boolean allowMixers;

    private int drinkCategoryId;

    private String imagePath;

    public String getImageUrl() {
        if (!TextUtils.isEmpty(imagePath) && !imagePath.startsWith("http")) {
            return prePendBaseUri();
        }
        return imagePath;
    }

    @NotNull
    private String prePendBaseUri() {
        return (imagePath.startsWith("/") ? ApplicationModule.BASE_URL_WITHOUT_SLASH : ApplicationModule.BASE_URL_WITHOUT_SLASH) + imagePath;
    }

    public List<DrinkOptionCategory> getAvailableOptions() {
        if (mixerCategories == null) {
            mixerCategories = new ArrayList<>();
        }
        return mixerCategories;
    }
}
