package com.sysadminanywhere.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Nav;

import java.util.List;
import java.util.function.Supplier;

/** A shared, keyboard-accessible hardware category browser. */
public class HardwareExplorer extends Div {

    public record Section(String id, String title, Supplier<Component> content) { }

    private final Div detail = new Div();
    private Button selected;

    public HardwareExplorer(String navigationLabel, List<Section> sections) {
        addClassName("hardware-explorer");
        Nav navigation = new Nav();
        navigation.addClassName("hardware-explorer-nav");
        navigation.getElement().setAttribute("aria-label", navigationLabel);
        detail.addClassName("hardware-explorer-detail");

        for (Section section : sections) {
            Button button = new Button(section.title());
            button.addClassName("hardware-explorer-link");
            button.addClickListener(event -> {
                select(button, section);
                button.getUI().ifPresent(ui -> button.getElement()
                        .executeJs("this.scrollIntoView({block: 'nearest', inline: 'center'})"));
            });
            navigation.add(button);
            if (selected == null) select(button, section);
        }
        add(navigation, detail);
    }

    private void select(Button button, Section section) {
        if (selected != null) {
            selected.removeClassName("selected");
            selected.getElement().removeAttribute("aria-current");
        }
        selected = button;
        button.addClassName("selected");
        button.getElement().setAttribute("aria-current", "page");
        detail.removeAll();
        H2 title = new H2(section.title());
        title.addClassName("hardware-explorer-title");
        detail.add(title, section.content().get());
    }
}
