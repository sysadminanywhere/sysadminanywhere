package com.sysadminanywhere.views.reports;

import com.sysadminanywhere.model.ReportItem;
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

import java.util.ArrayList;
import java.util.List;

@PageTitle("Group Reports")
@Route(value = "reports/groups", layout = MainLayout.class)
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
                    ui.navigate("reports/report?entry=group&filter=" + event.getValue().getFilter() + "&columns=" + event.getValue().getColumns()));
        });

        add(listBox);
    }

    private void addReports() {
        listBox.clear();

        List<ReportItem> reports = new ArrayList<>();
        reports.add(new ReportItem("Groups", "All groups", "","cn,description"));
        reports.add(new ReportItem("Domain security", "Domain security groups", "(groupType:1.2.840.113556.1.4.803:=2147483652)","cn,description"));
        reports.add(new ReportItem("Global distribution", "Global distribution groups", "(groupType:1.2.840.113556.1.4.803:=2)(!(groupType:1.2.840.113556.1.4.803:=2147483648))","cn,description"));
        reports.add(new ReportItem("Global security", "Global security groups", "(groupType:1.2.840.113556.1.4.803:=2147483650)","cn,description"));
        reports.add(new ReportItem("Universal security", "Universal security groups", "(groupType:1.2.840.113556.1.4.803:=2147483656)", "cn,description"));
        reports.add(new ReportItem("Created", "Created dates", "", "cn,description,created"));

        listBox.setItems(reports);
    }

}