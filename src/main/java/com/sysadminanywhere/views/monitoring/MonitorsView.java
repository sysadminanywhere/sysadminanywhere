package com.sysadminanywhere.views.monitoring;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PageTitle("Monitors")
@Route(value = "monitoring/monitors")
@PermitAll
@Uses(Icon.class)
public class MonitorsView extends Div {
}
