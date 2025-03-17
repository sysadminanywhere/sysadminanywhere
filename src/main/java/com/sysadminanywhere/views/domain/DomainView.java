package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.apache.directory.api.ldap.model.entry.Entry;

@PageTitle("Domain")
@Route(value = "domain/info")
@PermitAll
public class DomainView extends VerticalLayout {

    private final LdapService ldapService;

    public DomainView(LdapService ldapService) {
        this.ldapService = ldapService;

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");

        //Entry entry = ldapService.getDomainEntry();

        add(lblDomain, lblDistinguishedName);
    }

}