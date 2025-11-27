package com.sysadminanywhere.control;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.Lumo;

public class ThemeSwitcher extends HorizontalLayout {

    private final Checkbox toggle = new Checkbox("Dark mode");

    public ThemeSwitcher() {
        add(toggle);

        toggle.addValueChangeListener(e -> {
            boolean dark = e.getValue();
            applyTheme(dark);

            UI.getCurrent().getPage().executeJs(
                    "localStorage.setItem('theme', $0);", dark ? "dark" : "light"
            );
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI ui = attachEvent.getUI();
        ui.getPage().executeJs("return localStorage.getItem('theme');")
                .then(String.class, theme -> {
                    boolean dark = "dark".equals(theme);

                    applyTheme(dark);

                    toggle.setValue(dark);
                });
    }

    private void applyTheme(boolean dark) {
        UI ui = UI.getCurrent();

        if (dark) {
            ui.getElement().setAttribute("theme", Lumo.DARK);
        } else {
            ui.getElement().setAttribute("theme", Lumo.LIGHT);
        }
    }

}