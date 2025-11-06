package com.sysadminanywhere.views.error;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Error")
@Route(value = "error")
@PermitAll
public class ErrorView extends VerticalLayout {

    public ErrorView() {
        add(new H1("Domain Service Unavailable"));
        add(new Paragraph("Failed to connect to Domain service"));

        Anchor docsPage = new Anchor("https://docs.sysadminanywhere.com/", "Documentation", AnchorTarget.BLANK);
        add(docsPage);
    }

}