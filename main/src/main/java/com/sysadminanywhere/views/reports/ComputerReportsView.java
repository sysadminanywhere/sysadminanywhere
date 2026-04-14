package com.sysadminanywhere.views.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.model.ReportItem;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@RolesAllowed("ADMIN")
@PageTitle("computer_reports_view.title")
@Route(value = "reports/computers")
@Uses(Icon.class)
public class ComputerReportsView extends Div {

    ListBox<ReportItem> listBox = new ListBox<>();

    public ComputerReportsView() {

        listBox.setRenderer(new ComponentRenderer<>(item -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);

            Icon icon = new Icon(VaadinIcon.FILE_TABLE);

            Span name = new Span(item.getName());
            Span description = new Span(item.getDescription());
            description.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)");

            VerticalLayout column = new VerticalLayout(name, description);
            column.setPadding(false);
            column.setSpacing(false);

            row.add(icon, column);
            row.getStyle().set("line-height", "var(--lumo-line-height-m)");
            return row;
        }));

        addReports();

        listBox.addValueChangeListener(event ->
        {
            event.getSource().getUI().ifPresent(ui ->
                    ui.navigate("reports/report?entry=computers&id=" + event.getValue().getId()));
        });

        add(listBox);
    }

    private void addReports() {
        listBox.clear();

        Resource resource = new ClassPathResource("reports/computers.json");
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String json = reader.lines().collect(Collectors.joining("\n"));
            ReportItem[] reports = new ObjectMapper().readValue(json, ReportItem[].class);
            listBox.setItems(reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reports from computers.json", e);
        }
    }

}
