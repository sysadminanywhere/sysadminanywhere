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

@PageTitle("User Reports")
@Route(value = "reports/users", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class UserReportsView extends Div {

    ListBox<ReportItem> listBox = new ListBox<>();

    public UserReportsView() {

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
                    ui.navigate("reports/report?entry=user&filter=" + event.getValue().getFilter() + "&columns=" + event.getValue().getColumns()));
        });

        add(listBox);
    }

    private void addReports() {
        listBox.clear();

        List<ReportItem> reports = new ArrayList<>();
        reports.add(new ReportItem("Users", "All users", "", "cn,description"));
        reports.add(new ReportItem("Change password at next logon", "Users must change password at next logon", "(pwdLastSet=0)","cn,description"));
        reports.add(new ReportItem("Disabled", "Disabled users", "(userAccountControl:1.2.840.113556.1.4.803:=2)","cn,description"));
        reports.add(new ReportItem("Locked", "Locked out users", "(userAccountControl:1.2.840.113556.1.4.803:=10)","cn,description"));
        reports.add(new ReportItem("Password never expires", "Password never expires users", "(userAccountControl:1.2.840.113556.1.4.803:=65536)", "cn,description"));
        reports.add(new ReportItem("Created", "Created dates", "", "cn,description,created"));
        reports.add(new ReportItem("Logon scripts", "Logon scripts for users", "", "cn,description,scriptPath"));
        reports.add(new ReportItem("Profile paths", "Profile paths for users", "", "cn,description,profilePath"));
        reports.add(new ReportItem("Home folders", "Home folders for users", "", "cn,description,homeDirectory"));
        reports.add(new ReportItem("Last logon", "Users with last logon dates", "", "cn,description,lastLogon"));
        //reports.add(new ReportItem("Without manager", "Users without managers", "(!manager=*)", "cn,description"));

        listBox.setItems(reports);
    }

}