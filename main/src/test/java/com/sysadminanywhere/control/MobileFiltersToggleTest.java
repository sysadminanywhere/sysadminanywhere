package com.sysadminanywhere.control;

import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileFiltersToggleTest {
    @Test
    void exposesAndUpdatesExpandedState() {
        Div filters = new Div();
        MobileFiltersToggle toggle = new MobileFiltersToggle("Filters", filters);

        assertEquals("false", toggle.getElement().getAttribute("aria-expanded"));
        assertEquals(filters.getId().orElseThrow(), toggle.getElement().getAttribute("aria-controls"));

        toggle.click();
        assertTrue(filters.hasClassName("visible"));
        assertEquals("true", toggle.getElement().getAttribute("aria-expanded"));

        toggle.click();
        assertFalse(filters.hasClassName("visible"));
        assertEquals("false", toggle.getElement().getAttribute("aria-expanded"));
    }
}
