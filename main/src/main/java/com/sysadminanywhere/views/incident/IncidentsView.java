package com.sysadminanywhere.views.incident;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed("ADMIN")
@PageTitle("incidents")
@Route("incidents")
public class IncidentsView extends VerticalLayout {
}
