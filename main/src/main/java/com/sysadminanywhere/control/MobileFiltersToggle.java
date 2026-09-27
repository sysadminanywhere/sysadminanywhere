package com.sysadminanywhere.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;

import java.util.UUID;

/** A keyboard-accessible toggle for the filter panel on narrow screens. */
public class MobileFiltersToggle extends Button {
    public MobileFiltersToggle(String label, Component filters) {
        super(label);
        Icon icon = new Icon("lumo", "plus");
        setIcon(icon);
        setWidthFull();
        addClassName("mobile-filters");
        addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        String filterId = filters.getId().orElseGet(() -> "filters-" + UUID.randomUUID());
        filters.setId(filterId);
        getElement().setAttribute("aria-controls", filterId);
        getElement().setAttribute("aria-expanded", "false");
        addClickListener(event -> {
            boolean expanded = !filters.getClassNames().contains("visible");
            if (expanded) {
                filters.addClassName("visible");
            } else {
                filters.removeClassName("visible");
            }
            icon.getElement().setAttribute("icon", expanded ? "lumo:minus" : "lumo:plus");
            getElement().setAttribute("aria-expanded", Boolean.toString(expanded));
        });
    }
}
