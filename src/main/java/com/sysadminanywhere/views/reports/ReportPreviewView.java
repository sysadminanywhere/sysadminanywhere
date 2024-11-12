package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.vaadin.reports.PrintPreviewReport;

import java.util.List;
import java.util.Map;

@PageTitle("Report")
@Route(value = "reports/report", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ReportPreviewView extends Div implements BeforeEnterObserver {

    private String filter;
    private final LdapService ldapService;
    PrintPreviewReport<Entry> report = new PrintPreviewReport<>(Entry.class);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if(!parametersMap.isEmpty()) {
            filter = parametersMap.get("filter").get(0);
            updateView();
        }
    }

    public ReportPreviewView(LdapService ldapService) {
        this.ldapService = ldapService;

        add(report);
    }

    private void updateView() {
        report.setItems(ldapService.search(filter));
    }

}