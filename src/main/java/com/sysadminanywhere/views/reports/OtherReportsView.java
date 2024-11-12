package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Other Reports")
@Route(value = "reports/others", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class OtherReportsView extends Div {

}