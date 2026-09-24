package com.sysadminanywhere.views.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.model.ReportItem;
import com.sysadminanywhere.service.LocaleService;
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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "reports/contacts")
@Uses(Icon.class)
public class ContactReportsView extends Div implements HasDynamicTitle {

    private final ListBox<ReportItem> listBox = new ListBox<>();
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public ContactReportsView(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;

        listBox.setRenderer(new ComponentRenderer<>(item -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);

            Icon icon = new Icon(VaadinIcon.FILE_TABLE);
            Span name = new Span(item.getName());
            Span description = new Span(item.getDescription());
            description.getStyle().set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)");

            VerticalLayout column = new VerticalLayout(name, description);
            column.setPadding(false);
            column.setSpacing(false);
            row.add(icon, column);
            row.getStyle().set("line-height", "var(--lumo-line-height-m)");
            return row;
        }));

        addReports();
        listBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                event.getSource().getUI().ifPresent(ui ->
                        ui.navigate("reports/report?entry=contacts&id=" + event.getValue().getId()));
            }
        });
        add(listBox);
    }

    private void addReports() {
        try (InputStream inputStream = new ClassPathResource("reports/contacts.json").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String json = reader.lines().collect(Collectors.joining("\n"));
            ReportItem[] reports = new ObjectMapper().readValue(json, ReportItem[].class);
            for (ReportItem report : reports) {
                report.setName(getMessage(report.getName()));
                report.setDescription(getMessage(report.getDescription()));
                String[] names = report.getNames();
                for (int i = 0; i < names.length; i++) {
                    names[i] = getMessage(names[i]);
                }
            }
            listBox.setItems(reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load reports from contacts.json", e);
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return getMessage("contact_reports_view.title");
    }
}
