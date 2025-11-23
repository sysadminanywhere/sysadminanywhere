package com.sysadminanywhere.views.management.container;

import com.sysadminanywhere.control.MenuControl;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("admins")
@PageTitle("Containers")
@Route(value = "management/containers")
@PermitAll
@Uses(Icon.class)
public class ContainersView extends Div implements MenuControl {

    @Override
    public MenuBar getMenu() {
        return new MenuBar();
    }

}