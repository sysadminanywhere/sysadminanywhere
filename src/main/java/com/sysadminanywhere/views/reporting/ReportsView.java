package com.sysadminanywhere.views.reporting;

import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.PageImpl;
import org.vaadin.reports.PrintPreviewReport;

@PageTitle("Reports")
@Route(value = "reporting/reports", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ReportsView extends Div {

    private final ComputersService computersService;

    public ReportsView(ComputersService computersService) {
        this.computersService = computersService;
        PrintPreviewReport<ComputerEntry> report = new PrintPreviewReport<>(ComputerEntry.class, "cn", "description");
        report.setItems(computersService.getAll());
        add(report);
    }

}