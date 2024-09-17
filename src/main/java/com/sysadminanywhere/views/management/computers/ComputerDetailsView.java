package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Computer details")
@Route(value = "management/computers/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ComputerDetailsView extends Div implements BeforeEnterObserver {

    private String id;

    private final ComputersService computersService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            ComputerEntry computer = computersService.getByCN(id);

            if (computer != null) {
                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());
            }
        }
    }

    public ComputerDetailsView(ComputersService computersService) {
        this.computersService = computersService;

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.setWidth("100%");
        verticalLayout.setMaxWidth("800px");
        verticalLayout.setHeight("min-content");

        lblName.setWidth("100%");
        lblDescription.setWidth("100%");

        add(verticalLayout);

        verticalLayout.add(lblName);
        verticalLayout.add(lblDescription);
    }

}
