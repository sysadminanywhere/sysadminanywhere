package com.sysadminanywhere.control;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.theme.lumo.Lumo;

public class ThemeSwitcher extends Button {

    private boolean dark = false; // default (будет перезаписано после attach)

    public ThemeSwitcher() {
        getStyle().set("font-size", "1.4rem");

        addClickListener(e -> {
            dark = !dark;
            applyTheme(dark);
            saveTheme(dark);
            updateIcon();
        });

        updateIcon();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI ui = attachEvent.getUI();
        ui.getPage().executeJs("return localStorage.getItem('theme');")
                .then(String.class, theme -> {
                    dark = "dark".equals(theme);
                    applyTheme(ui, dark);
                    updateIcon();
                });
    }

    private void updateIcon() {
        setText(dark ? "🌙" : "☀️");
    }

    private void applyTheme(boolean dark) {
        applyTheme(UI.getCurrent(), dark);
    }

    private void applyTheme(UI ui, boolean dark) {
        ui.getElement().getThemeList().remove(Lumo.DARK);
        ui.getElement().getThemeList().remove(Lumo.LIGHT);
        ui.getElement().getThemeList().add(dark ? Lumo.DARK : Lumo.LIGHT);
        ui.getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0);",
                dark ? Lumo.DARK : Lumo.LIGHT
        );
    }

    private void saveTheme(boolean dark) {
        UI.getCurrent().getPage().executeJs(
                "localStorage.setItem('theme', $0);",
                dark ? "dark" : "light"
        );
    }

}
