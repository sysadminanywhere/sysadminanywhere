package com.sysadminanywhere.views.management.computers;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.RadialBarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.radialbar.builder.HollowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.TickPlacement;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RolesAllowed("admins")
@PageTitle("Performance")
@Route(value = "management/computers/:id?/performance")
@PermitAll
@Uses(Icon.class)
public class ComputerPerformanceView extends Div implements BeforeEnterObserver {

    private volatile boolean pageActive = true;

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

    private ApexCharts chartMemory;
    private ApexCharts chartDisk;
    private ApexCharts chartProcessor;

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

                new Thread(() -> {
                    while (pageActive) {
                        try {
                            getUI().ifPresent(ui -> ui.access(() -> {
                                if (pageActive) {
                                    run();
                                }
                            }));
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }).start();

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

        getStoredTheme().thenAccept(v -> {
            boolean isDarkTheme = false;

            if (v.contains("dark"))
                isDarkTheme = true;

            String theme = isDarkTheme ? "dark" : "light";
            String foreColor = isDarkTheme ? "white" : "black";

            chartProcessor = ApexChartsBuilder.get()
                 .withTooltip(TooltipBuilder.get()
                        .withFillSeriesColor(false)
                        .withTheme(theme).build())
                 .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withForeColor(foreColor)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
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

        chartMemory = ApexChartsBuilder.get()
                .withTooltip(TooltipBuilder.get()
                        .withFillSeriesColor(false)
                        .withTheme(theme).build())
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withForeColor(foreColor)
                        .withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .withZoom(ZoomBuilder.get().withEnabled(false).build())
                        .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
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

        chartDisk = ApexChartsBuilder.get()
                .withTooltip(TooltipBuilder.get()
                        .withFillSeriesColor(false)
                        .withTheme(theme).build())
                .withChart(ChartBuilder.get()
                        .withType(Type.RADIALBAR)
                        .withForeColor(foreColor)
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
        });

    }

    private void run() {

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
            if(chartMemory != null)
                chartMemory.updateSeries(seriesMemory);

            if(chartProcessor != null)
                chartProcessor.updateSeries(seriesProcessor);

            if(chartDisk != null)
                chartDisk.updateSeries(disk.doubleValue());
        });

    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        pageActive = false;
        super.onDetach(detachEvent);
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