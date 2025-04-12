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
import jakarta.annotation.security.PermitAll;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;

@PageTitle("Group Reports")
@Route(value = "reports/groups")
@PermitAll
@Uses(Icon.class)
public class GroupReportsView extends Div {

    ListBox<ReportItem> listBox = new ListBox<>();

    public GroupReportsView() {

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
                    ui.navigate("reports/report?entry=groups&id=" + event.getValue().getId()));
        });

        add(listBox);
    }

    @SneakyThrows
    private void addReports() {
        listBox.clear();

        Resource resource = new ClassPathResource("reports/groups.json");
        InputStream inputStream = resource.getInputStream();
        String json = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));

        ReportItem[] reports = new ObjectMapper().readValue(json, ReportItem[].class);

        listBox.setItems(reports);
    }

}