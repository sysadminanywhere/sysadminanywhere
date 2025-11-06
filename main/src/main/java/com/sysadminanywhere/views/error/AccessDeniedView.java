package com.sysadminanywhere.views.error;

import com.sysadminanywhere.views.domain.DashboardView;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("access-denied")
@PageTitle("Access Denied")
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        add(
                new H1("403 — Access Denied"),
                new Paragraph("You do not have permission to view this page."),
                new RouterLink("Back to Dashboard", DashboardView.class)
        );
    }
}