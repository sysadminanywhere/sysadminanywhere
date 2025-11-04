package com.sysadminanywhere.views.domain;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Domain Error")
@Route(value = "domain/error")
@PermitAll
public class DomainErrorView extends VerticalLayout {

    public DomainErrorView() {
        setPadding(true);
        setMargin(true);

        H3 lblDomain = new H3();
        lblDomain.setText("Error");
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText("Domain service is unavailable!");
        lblDistinguishedName.setWidth("100%");
        lblDistinguishedName.getStyle().setMarginBottom("20px");

        add(lblDomain, lblDistinguishedName);

    }

}
