package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.GroupEntry;
import com.sysadminanywhere.model.UserEntry;
import com.sysadminanywhere.service.*;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.vaadin.reports.PrintPreviewReport;

import java.util.List;
import java.util.Map;

@PageTitle("Report")
@Route(value = "reports/report", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ReportPreviewView extends Div implements BeforeEnterObserver {

    private String filter;
    private String[] columns;
    private String entry;

    private final ComputersService computersService;
    private final UsersService usersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (!parametersMap.isEmpty()
                && parametersMap.containsKey("entry")
                && parametersMap.containsKey("filter")
                && parametersMap.containsKey("columns")) {

            entry = parametersMap.get("entry").get(0);
            filter = parametersMap.get("filter").get(0);
            columns = parametersMap.get("columns").get(0).split(",");

            updateView();
        }
    }

    public ReportPreviewView(ComputersService computersService, UsersService usersService, GroupsService groupsService, PrintersService printersService) {
        this.computersService = computersService;
        this.usersService = usersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
    }

    private void updateView() {
        switch (entry.toLowerCase()){
            case "computer":
                add(computerReports());
                break;
            case "user":
                add(userReports());
                break;
            case "group":
                add(groupReports());
                break;
        }
    }

    private PrintPreviewReport computerReports() {
        PrintPreviewReport<ComputerEntry> report = new PrintPreviewReport<>(ComputerEntry.class, columns);

        report.getReportBuilder()
                .setMargins(20, 20, 20, 20)
                .setTitle("Computers")
                .setPrintBackgroundOnOddRows(true);

        report.setItems(computersService.getAll(filter));

        return report;
    }

    private PrintPreviewReport userReports() {
        PrintPreviewReport<UserEntry> report = new PrintPreviewReport<>(UserEntry.class, columns);

        report.getReportBuilder()
                .setMargins(20, 20, 20, 20)
                .setTitle("Users")
                .setPrintBackgroundOnOddRows(true);

        report.setItems(usersService.getAll(filter));

        return report;
    }

    private PrintPreviewReport groupReports() {
        PrintPreviewReport<GroupEntry> report = new PrintPreviewReport<>(GroupEntry.class, columns);

        report.getReportBuilder()
                .setMargins(20, 20, 20, 20)
                .setTitle("Groups")
                .setPrintBackgroundOnOddRows(true);

        report.setItems(groupsService.getAll(filter));

        return report;
    }

}