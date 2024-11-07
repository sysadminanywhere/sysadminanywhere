package com.sysadminanywhere.domain;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.theme.lumo.LumoIcon;

public class MenuHelper {

    public static MenuItem createIconItem(MenuBar menu, VaadinIcon iconName, String text, ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        Icon icon = new Icon(iconName);
        MenuItem item = menu.addItem(icon, clickListener);
        if (text != null) {
            item.add(new Text(text));
        }
        return item;
    }

}
