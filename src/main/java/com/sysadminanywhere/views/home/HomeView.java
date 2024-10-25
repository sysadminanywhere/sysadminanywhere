package com.sysadminanywhere.views.home;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.model.*;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.ResolveService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;
import org.apache.directory.api.ldap.model.entry.Entry;

import java.util.List;
import java.util.Map;

@PageTitle("Home")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "home", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@PermitAll
public class HomeView extends VerticalLayout {

    private final LdapService ldapService;

    public HomeView(LdapService ldapService) {
        this.ldapService = ldapService;
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");

        List<ComputerEntry> computers = new ResolveService<>(ComputerEntry.class).getADList(ldapService.search("(objectClass=computer)"));
        List<UserEntry> users = new ResolveService<>(UserEntry.class).getADList(ldapService.search("(&(objectClass=user)(objectCategory=person))"));
        List<GroupEntry> groups = new ResolveService<>(GroupEntry.class).getADList(ldapService.search("(objectClass=group)"));
        List<PrinterEntry> printers = new ResolveService<>(PrinterEntry.class).getADList(ldapService.search("(objectClass=printQueue)"));
        List<ContactEntry> contacts = new ResolveService<>(ContactEntry.class).getADList(ldapService.search("(&(objectClass=contact)(objectCategory=person))"));

        ApexCharts chartSummary = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.BAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .withColumnWidth("55%")
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false).build())
                .withStroke(StrokeBuilder.get()
                        .withShow(true)
                        .withWidth(2.0)
                        .withColors("transparent")
                        .build())
                .withSeries(new Series<>("Computers", computers.size()),
                        new Series<>("Users", users.size()),
                        new Series<>("Groups", groups.size()),
                        new Series<>("Printers", printers.size()),
                        new Series<>("Contacts", contacts.size()))
                .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
        chartSummary.setHeight("300px");
        chartSummary.setWidth("300px");
        chartSummary.setTitle(TitleSubtitleBuilder.get().withText("Summary").build());

        ApexCharts chartUsers = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.BAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .withColumnWidth("55%")
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false).build())
                .withStroke(StrokeBuilder.get()
                        .withShow(true)
                        .withWidth(2.0)
                        .withColors("transparent")
                        .build())
                .withSeries(new Series<>("Number of users", users.size()),
                        new Series<>("Disabled", users.stream().filter(c -> c.isDisabled()).count()),
                        new Series<>("Locked", users.stream().filter(c -> c.isLocked()).count()),
                        new Series<>("Expired", users.size()),
                        new Series<>("Never expires", users.size()))
                .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
        chartUsers.setHeight("300px");
        chartUsers.setWidth("300px");
        chartUsers.setTitle(TitleSubtitleBuilder.get().withText("Users").build());

        ApexCharts chartComputers = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.BAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .withColumnWidth("55%")
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false).build())
                .withStroke(StrokeBuilder.get()
                        .withShow(true)
                        .withWidth(2.0)
                        .withColors("transparent")
                        .build())
                .withSeries(new Series<>("Number of computers", computers.size()),
                        new Series<>("Disabled", computers.size()),
                        new Series<>("Workstations", computers.size()),
                        new Series<>("Domain controllers", computers.size()))
                .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
        chartComputers.setHeight("300px");
        chartComputers.setWidth("300px");
        chartComputers.setTitle(TitleSubtitleBuilder.get().withText("Computers").build());

        ApexCharts chartGroups = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.BAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withBar(BarBuilder.get()
                                .withHorizontal(false)
                                .withColumnWidth("55%")
                                .build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false).build())
                .withStroke(StrokeBuilder.get()
                        .withShow(true)
                        .withWidth(2.0)
                        .withColors("transparent")
                        .build())
                .withSeries(new Series<>("Number of groups", groups.size()),
                        new Series<>("Security", groups.size()),
                        new Series<>("Distribution", groups.size()),
                        new Series<>("Built-In", groups.size()))
                .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
        chartGroups.setHeight("300px");
        chartGroups.setWidth("300px");
        chartGroups.setTitle(TitleSubtitleBuilder.get().withText("Groups").build());

        verticalLayout.add(lblDomain, lblDistinguishedName, chartSummary, chartUsers, chartComputers, chartGroups);

        add(verticalLayout);
    }

}