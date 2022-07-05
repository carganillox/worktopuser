package org.drinklink.app.model;

import org.drinklink.app.model.request.OrderItemRequest;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DrinkCategory extends NamedObject {

    public List<Drink> drinks;

    public transient DrinkCategory parentCategory;

    public List<DrinkCategory> subCategories;

    private Integer parentCategoryId;

    private String tag;

    private int iconId;

    public List<DrinkCategory> getParentCategories(ArrayList<DrinkCategory> parentCategories) {
        if (parentCategory != null) {
            parentCategory.getParentCategories(parentCategories);
        }
        parentCategories.add(this);
        return parentCategories;
    }

    public void setParentCategoryFromMenu() {
        if (subCategories == null) {
            return;
        }
        for (DrinkCategory sub : subCategories) {
            sub.setParentCategory(this);
            sub.setParentCategoryFromMenu();
        }
    }

    private static int mix = 0;

    public int getIconId() {
        return iconId;
    }

    public boolean isEmpty() {
        if (!drinks.isEmpty()) {
            return false;
        }
        return areSubcategoriesEmpty();

    }

    public boolean areSubcategoriesEmpty() {
        if (subCategories == null) {
            return true;
        }
        for (DrinkCategory sub : subCategories) {
            boolean empty = sub.isEmpty();
            if (!empty) {
                return false;
            }
        }
        return true;
    }

    public Drink matchItem(OrderItemRequest item) {
        for (Drink drink : getDrinks()) {
            if (drink.getId() == item.getDrinkId()) {
                return drink;
            }
        }
        for (DrinkCategory category : subCategories) {
            Drink drink = category.matchItem(item);
            if (drink != null) {
                return drink;
            }
        }
        return null;
    }
}
