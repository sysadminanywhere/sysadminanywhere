package com.sysadminanywhere.views.management.users;

import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.UsersService;
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

@PageTitle("User details")
@Route(value = "management/users/:userId?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class UserDetailsView extends Div implements BeforeEnterObserver {

    private String userId;

    private final UsersService usersService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        userId = event.getRouteParameters().get("userId").
                orElse(null);

        if (userId != null) {
            UserEntry user = usersService.getByCN(userId);

            if (user != null) {
                lblName.setText(user.getCn());
                lblDescription.setText(user.getDescription());
            }
        }
    }

    public UserDetailsView(UsersService usersService) {
        this.usersService = usersService;

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
