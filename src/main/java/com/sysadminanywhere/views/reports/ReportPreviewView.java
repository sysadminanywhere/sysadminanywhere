package com.sysadminanywhere.views.reports;

import ar.com.fdvs.dj.domain.AutoText;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.model.ad.ComputerEntry;
import com.sysadminanywhere.model.ad.GroupEntry;
import com.sysadminanywhere.model.ReportItem;
import com.sysadminanywhere.model.ad.UserEntry;
import com.sysadminanywhere.service.*;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.springframework.core.io.ClassPathResource;
import org.vaadin.reports.PrintPreviewReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PageTitle("Report")
@Route(value = "reports/report")
@PermitAll
@Uses(Icon.class)
public class ReportPreviewView extends Div implements BeforeEnterObserver {

    private String id;
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
                && parametersMap.containsKey("id")) {

            entry = parametersMap.get("entry").get(0);
            id = parametersMap.get("id").get(0);

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

        ReportItem reportItem = null;

        try {
            File resource = new ClassPathResource("reports/" + entry.toLowerCase() + ".json").getFile();
            String json = new String(Files.readAllBytes(resource.toPath()));
            ReportItem[] reports = new ObjectMapper().readValue(json, ReportItem[].class);

            Optional<ReportItem> item = Arrays.stream(reports).filter(c -> c.getId().equalsIgnoreCase(id)).findFirst();
            if (item.isPresent())
                reportItem = item.get();
            else
                return;

        } catch (IOException e) {
            return;
        }

        VerticalLayout verticalLayout = new VerticalLayout();

        switch (entry.toLowerCase()) {
            case "computers":
                verticalLayout = computerReports(reportItem);
                break;
            case "users":
                verticalLayout = userReports(reportItem);
                break;
            case "groups":
                verticalLayout = groupReports(reportItem);
                break;
        }

        add(verticalLayout);
    }

    private VerticalLayout computerReports(ReportItem reportItem) {
        PrintPreviewReport<ComputerEntry> report = new PrintPreviewReport<>(ComputerEntry.class, reportItem.getColumns());

        report = getTemplate(report, reportItem.getName(), reportItem.getDescription());

        SerializableSupplier<List<? extends ComputerEntry>> itemsSupplier = () -> computersService.getAll(reportItem.getFilter());
        report.setItems(itemsSupplier.get());

        return new DownloadMenu<>(ComputerEntry.class).getDownloadMenu(report, reportItem.getId(), itemsSupplier);
    }

    private VerticalLayout userReports(ReportItem reportItem) {
        PrintPreviewReport<UserEntry> report = new PrintPreviewReport<>(UserEntry.class, reportItem.getColumns());

        report = getTemplate(report, reportItem.getName(), reportItem.getDescription());

        SerializableSupplier<List<? extends UserEntry>> itemsSupplier = () -> usersService.getAll(reportItem.getFilter());
        report.setItems(itemsSupplier.get());

        return new DownloadMenu<>(UserEntry.class).getDownloadMenu(report, reportItem.getId(), itemsSupplier);
    }

    private VerticalLayout groupReports(ReportItem reportItem) {
        PrintPreviewReport<GroupEntry> report = new PrintPreviewReport<>(GroupEntry.class, reportItem.getColumns());

        report = getTemplate(report, reportItem.getName(), reportItem.getDescription());

        SerializableSupplier<List<? extends GroupEntry>> itemsSupplier = () -> groupsService.getAll(reportItem.getFilter());
        report.setItems(itemsSupplier.get());

        return new DownloadMenu<>(GroupEntry.class).getDownloadMenu(report, reportItem.getId(), itemsSupplier);
    }

    private PrintPreviewReport getTemplate(PrintPreviewReport report, String title, String subtitle) {
        report.getReportBuilder()
                .setMargins(20, 20, 20, 20)
                .setTitle(title + " / " + subtitle)
                .setPrintBackgroundOnOddRows(true)
                .addAutoText("Sysadmin Anywhere", AutoText.POSITION_FOOTER, AutoText.ALIGMENT_LEFT, 200)
                .addAutoText("sysadminanywhere.com", AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_CENTER, 200)
                .addAutoText(AutoText.AUTOTEXT_PAGE_X, AutoText.POSITION_FOOTER, AutoText.ALIGNMENT_RIGHT);

        return report;
    }

}