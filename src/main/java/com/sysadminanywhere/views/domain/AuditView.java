package com.sysadminanywhere.views.domain;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Audit")
@Route(value = "domain/audit")
@PermitAll
public class AuditView extends Div {
}
