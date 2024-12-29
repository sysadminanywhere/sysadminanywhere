package com.sysadminanywhere.control;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class Card extends Composite<Div> implements HasComponents, HasSize {
    Div div = new Div();
    Div content = new Div();
    H4 title = new H4();
    String titleText;

    public Card(String titleText) {
        this.titleText = titleText;
    }

    @Override
    public Div initContent() {
        title.setText(titleText);
        title.addClassNames(LumoUtility.Background.CONTRAST_5,
                LumoUtility.TextColor.HEADER, LumoUtility.Padding.MEDIUM,
                LumoUtility.Border.BOTTOM,
                LumoUtility.BorderColor.CONTRAST_10);
        div.addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN, LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.BorderRadius.SMALL);
        content.addClassNames(LumoUtility.Flex.GROW,
                LumoUtility.Padding.SMALL);
        div.add(title, content);
        return div;
    }

    @Override
    public void add(Component... components) {
        content.add(components);
    }

    @Override
    public void remove(Component... components) {
        content.remove(components);
    }
}