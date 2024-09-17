package com.sysadminanywhere.views.management.contacts;

import com.sysadminanywhere.model.ContactEntry;
import com.sysadminanywhere.service.ContactsService;
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

@PageTitle("Contact details")
@Route(value = "management/contacts/:id?/details", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ContactDetailsView extends Div implements BeforeEnterObserver {

    private String id;

    private final ContactsService contactsService;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        if (id != null) {
            ContactEntry contact = contactsService.getByCN(id);

            if (contact != null) {
                lblName.setText(contact.getCn());
                lblDescription.setText(contact.getDescription());
            }
        }
    }

    public ContactDetailsView(ContactsService contactsService) {
        this.contactsService = contactsService;

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
