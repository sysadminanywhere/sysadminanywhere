package com.sysadminanywhere.views.account;

import com.sysadminanywhere.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Me")
@Route(value = "account/me")
@PermitAll
public class MeView extends VerticalLayout {

    private AuthenticatedUser authenticatedUser;

    public MeView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        Button logoutButton = new Button("Sign out");
        logoutButton.addClickListener(e -> {
            authenticatedUser.logout();
        });

        add(logoutButton);
    }

}