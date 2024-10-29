package com.sysadminanywhere.views.reporting;

import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Reports")
@Route(value = "reporting/reports", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ReportsView extends Div {
}
