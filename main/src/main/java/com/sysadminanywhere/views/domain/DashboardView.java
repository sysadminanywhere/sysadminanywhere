package com.sysadminanywhere.views.domain;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.common.directory.model.*;
import com.sysadminanywhere.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@PageTitle("Dashboard")
@Route(value = "")
@RouteAlias(value = "domain/dashboard")
@RouteAlias(value = "dashboard")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final String ColumnWidth = "55%";
    private final String ChartHeight = "300px";
    private final String ChartWidth = "400px";

    private final ComputersService computersService;
    private final UsersService usersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;
    private final ContactsService contactsService;

    public DashboardView(ComputersService computersService,
                         UsersService usersService,
                         GroupsService groupsService,
                         PrintersService printersService,
                         ContactsService contactsService) {

        this.computersService = computersService;
        this.usersService = usersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
        this.contactsService = contactsService;

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        List<ComputerEntry> computers = computersService.getAll();
        List<UserEntry> users = usersService.getAll();
        List<GroupEntry> groups = groupsService.getAll();
        List<PrinterEntry> printers = printersService.getAll();
        List<ContactEntry> contacts = contactsService.getAll();

        getStoredTheme().thenAccept(v -> {
            boolean isDarkTheme = false;

            if (v.contains("dark"))
                isDarkTheme = true;

            String theme = isDarkTheme ? "dark" : "light";
            String foreColor = isDarkTheme ? "white" : "black";

            ApexCharts chartSummary = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
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
            chartSummary.setHeight(ChartHeight);
            chartSummary.setWidth(ChartWidth);
            chartSummary.setTitle(TitleSubtitleBuilder.get().withText("Summary").build());

            ApexCharts chartUsers = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
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
                            new Series<>("Expired", users.stream().filter(c -> c.isExpired()).count()),
                            new Series<>("Never expires", users.stream().filter(c -> c.isNeverExpires()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartUsers.setHeight(ChartHeight);
            chartUsers.setWidth(ChartWidth);
            chartUsers.setTitle(TitleSubtitleBuilder.get().withText("Users").build());

            ApexCharts chartComputers = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
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
                            new Series<>("Disabled", computers.stream().filter(c -> c.isDisabled()).count()),
                            new Series<>("Workstations", computers.stream().filter(c -> c.isWorkstation()).count()),
                            new Series<>("Servers", computers.stream().filter(c -> c.isServer()).count()),
                            new Series<>("Domain controllers", computers.stream().filter(c -> c.isDomainController()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartComputers.setHeight(ChartHeight);
            chartComputers.setWidth(ChartWidth);
            chartComputers.setTitle(TitleSubtitleBuilder.get().withText("Computers").build());

            ApexCharts chartGroups = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
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
                            new Series<>("Security", groups.stream().filter(c -> c.isSecurity()).count()),
                            new Series<>("Distribution", groups.stream().filter(c -> c.isDistribution()).count()),
                            new Series<>("BuiltIn", groups.stream().filter(c -> c.isBuiltIn()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartGroups.setHeight(ChartHeight);
            chartGroups.setWidth(ChartWidth);
            chartGroups.setTitle(TitleSubtitleBuilder.get().withText("Groups").build());

            HorizontalLayout line = new HorizontalLayout();
            line.setWrap(true);
            line.add(chartSummary, chartUsers, chartComputers, chartGroups);

            verticalLayout.add(line);

            add(verticalLayout);

        });
    }

    private CompletableFuture<String> getStoredTheme() {
        CompletableFuture<String> future = new CompletableFuture<>();

        UI.getCurrent().getPage().executeJs("return localStorage.getItem('theme');")
                .then(String.class, theme -> {
                    future.complete(theme != null ? theme : "light");
                });

        return future;
    }

}