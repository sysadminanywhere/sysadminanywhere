package com.sysadminanywhere.views.home;

import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import jakarta.annotation.security.PermitAll;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "home", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@PermitAll
public class HomeView extends VerticalLayout {

    private final LdapService ldapService;

    public HomeView(LdapService ldapService) {
        this.ldapService = ldapService;
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");

        FormLayout formLayout = new FormLayout();

        TextField txtComputers = new TextField();
        txtComputers.setReadOnly(true);
        txtComputers.setValue(String.valueOf(ldapService.search("(objectClass=computer)").size()));
        formLayout.addFormItem(txtComputers, "Computers");

        TextField txtUsers = new TextField();
        txtUsers.setReadOnly(true);
        txtUsers.setValue(String.valueOf(ldapService.search("(&(objectClass=user)(objectCategory=person))").size()));
        formLayout.addFormItem(txtUsers, "Users");

        TextField txtGroups = new TextField();
        txtGroups.setReadOnly(true);
        txtGroups.setValue(String.valueOf(ldapService.search("(objectClass=group)").size()));
        formLayout.addFormItem(txtGroups, "Groups");

        TextField txtPrinters = new TextField();
        txtPrinters.setReadOnly(true);
        txtPrinters.setValue(String.valueOf(ldapService.search("(objectClass=printQueue)").size()));
        formLayout.addFormItem(txtPrinters, "Printers");

        TextField txtContacts = new TextField();
        txtContacts.setReadOnly(true);
        txtContacts.setValue(String.valueOf(ldapService.search("(&(objectClass=contact)(objectCategory=person))").size()));
        formLayout.addFormItem(txtContacts, "Contacts");

        verticalLayout.add(lblDomain, lblDistinguishedName, formLayout);

        add(verticalLayout);
    }

}
