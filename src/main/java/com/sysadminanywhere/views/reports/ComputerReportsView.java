package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.model.ReportItem;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.views.MainLayout;
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
import org.apache.directory.api.ldap.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Computer Reports")
@Route(value = "reports/computers", layout = MainLayout.class)
@PermitAll
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
            event.getSource().getUI().ifPresent(ui -> ui.navigate("reports/report?filter=" + event.getValue().getFilter()));
        });

        add(listBox);
    }

    private void addReports() {
        listBox.clear();

        List<ReportItem> reports = new ArrayList<>();
        reports.add(new ReportItem("Computers", "All computers", "(objectClass=computer)"));
        reports.add(new ReportItem("Controllers", "All controllers", "(&(objectCategory=computer)(primaryGroupID=516))"));
        reports.add(new ReportItem("Disabled", "Disabled computers", "(&(objectCategory=computer)(userAccountControl:1.2.840.113556.1.4.803:=2))"));
        reports.add(new ReportItem("Servers", "All servers", "(&(objectCategory=computer)(operatingSystem=Windows*Server*))"));
        reports.add(new ReportItem("Created", "Created dates", "(objectClass=computer)"));
        reports.add(new ReportItem("Last logon", "Computers with last logon dates", "(objectClass=computer)"));

        listBox.setItems(reports);
    }

}