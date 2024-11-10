package com.sysadminanywhere.views.management.computers;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.RadialBarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.radialbar.builder.HollowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.TickPlacement;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.MenuHelper;
import com.sysadminanywhere.model.ComputerEntry;
import com.sysadminanywhere.model.EventEntity;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.views.MainLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import jakarta.annotation.security.PermitAll;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@PageTitle("Performance")
@Route(value = "management/computers/:id?/performance", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class ComputerPerformanceView extends Div implements BeforeEnterObserver {

    private String id;
    private final ComputersService computersService;
    ComputerEntry computer;

    H3 lblName = new H3();
    H5 lblDescription = new H5();

    Binder<ComputerEntry> binder = new Binder<>(ComputerEntry.class);

    int processorStackSize = 10;
    int memoryStackSize = 10;

    List<Integer> processorStack = new ArrayList<>();
    List<Integer> memoryStack = new ArrayList<>();

    Long totalPhysicalMemory = 0L;

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);

        updateView();
    }

    private void updateView() {
        if (id != null) {
            computer = computersService.getByCN(id);

            if (computer != null) {
                binder.readBean(computer);

                lblName.setText(computer.getCn());
                lblDescription.setText(computer.getDescription());

                totalPhysicalMemory = computersService.getTotalPhysicalMemory(id);
            }
        }
    }

    public ComputerPerformanceView(ComputersService computersService) {
        this.computersService = computersService;

        for (int i = 0; i < processorStackSize; i++) {
            processorStack.add(0);
        }

        for (int i = 0; i < memoryStackSize; i++) {
            memoryStack.add(0);
        }

        addClassName("users-view");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidthFull();

        lblName.setText("Name");
        lblName.setWidth("100%");

        lblDescription.setText("Description");
        lblDescription.setWidth("100%");

        add(verticalLayout);

        VerticalLayout verticalLayout2 = new VerticalLayout(lblName, lblDescription);
        verticalLayout2.setWidth("70%");

        verticalLayout.add(verticalLayout2);

        final ApexCharts chartProcessor = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5).build()
                        ).build())
                .withXaxis(XAxisBuilder.get()
                        .withCategories("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                        .withMin(0.0)
                        .withMax(100.0)
                        .withTickPlacement(TickPlacement.BETWEEN)
                        .build())
                .withYaxis(YAxisBuilder.get().withMin(0.0).withMax(100.0).build())
                .withSeries(new Series<>("Processor", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)).build();

        chartProcessor.setHeight("300px");
        chartProcessor.setWidth("400px");
        chartProcessor.setTitle(TitleSubtitleBuilder.get().withText("Processor").build());

        // ===============

        final ApexCharts chartMemory = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5).build()
                        ).build())
                .withXaxis(XAxisBuilder.get()
                        .withCategories("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                        .withTickPlacement(TickPlacement.BETWEEN)
                        .build())
                .withYaxis(YAxisBuilder.get().withMin(0.0).withMax(100.0).build())
                .withSeries(new Series<>("Memory", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)).build();

        chartMemory.setHeight("300px");
        chartMemory.setWidth("400px");
        chartMemory.setTitle(TitleSubtitleBuilder.get().withText("Memory").build());

        HorizontalLayout line1 = new HorizontalLayout();
        line1.add(chartProcessor, chartMemory);

        final ApexCharts chartDisk = ApexChartsBuilder.get().withChart(ChartBuilder.get()
                        .withType(Type.RADIALBAR)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withPlotOptions(PlotOptionsBuilder.get()
                        .withRadialBar(RadialBarBuilder.get()
                                .withHollow(HollowBuilder.get()
                                        .withSize("70%")
                                        .build())
                                .build())
                        .build())
                .withSeries(0.0)
                .withLabels("C:")
                .build();

        chartDisk.setHeight("300px");
        chartDisk.setWidth("400px");
        chartDisk.setTitle(TitleSubtitleBuilder.get().withText("Disk").build());

        HorizontalLayout line2 = new HorizontalLayout();
        line2.add(chartDisk);

        verticalLayout.add(line1, line2);

        scheduler.scheduleAtFixedRate(() -> {

            Long availableBytes = computersService.getAvailableBytes(id);
            Long percent = ((totalPhysicalMemory - availableBytes) * 100) / totalPhysicalMemory;

            memoryStack.remove(0);
            memoryStack.add(percent.intValue());
            Series seriesMemory = new Series();
            seriesMemory.setData(memoryStack.toArray());


            Integer processorLoad = computersService.getProcessorLoad(id);

            processorStack.remove(0);
            processorStack.add(processorLoad);
            Series seriesProcessor = new Series();
            seriesProcessor.setData(processorStack.toArray());

            Integer disk = computersService.getDisk(id);

            if (getUI().isEmpty()) return;
            getUI().get().access(() -> {
                chartMemory.updateSeries(seriesMemory);
                chartProcessor.updateSeries(seriesProcessor);
                chartDisk.updateSeries(disk.doubleValue());
            });
        }, 1, 5, TimeUnit.SECONDS);
    }

}