package org.drinklink.app.ui.viewmodel;

import org.drinklink.app.model.request.OrderItemRequest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class SelectedDrinkPreviewItemTest {

    @Test
    public void incrementNegative() {
        SelectedDrinkPreviewItem preview = new SelectedDrinkPreviewItem(new OrderItemRequest());
        preview.increment(-1);
        assertEquals(0, preview.getCount());
    }

    @Test
    public void incrementMax() {
        SelectedDrinkPreviewItem preview = new SelectedDrinkPreviewItem(new OrderItemRequest());
        preview.increment(99);
        preview.increment(1);
        assertEquals(99, preview.getCount());
    }
}