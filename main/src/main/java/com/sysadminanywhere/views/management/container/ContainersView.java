package com.sysadminanywhere.views.management.container;

import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.control.MenuControl;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.tatu.Tree;

@RolesAllowed("admins")
@PageTitle("Containers")
@Route(value = "management/containers")
@PermitAll
@Uses(Icon.class)
public class ContainersView extends Div implements MenuControl {

    private final LdapService ldapService;

    public ContainersView(LdapService ldapService) {
        this.ldapService = ldapService;

        setHeightFull();

        Tree<Container> tree = new Tree<>(Container::getName);
        tree.setHeightFull();

        Containers containers = ldapService.getContainers();
        tree.setItems(containers.getRootContainers(), containers::getChildContainers);

        SplitLayout splitLayout = new SplitLayout(tree, new VerticalLayout());
        tree.setMinWidth("250px");
        tree.setMaxWidth("400px");
        splitLayout.setHeightFull();

        add(splitLayout);
    }

    @Override
    public MenuBar getMenu() {
        return new MenuBar();
    }

}