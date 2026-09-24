package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareItem;
import com.sysadminanywhere.common.inventory.model.OperatingSystemCount;
import com.sysadminanywhere.common.inventory.model.InventoryCoverage;
import com.sysadminanywhere.common.inventory.model.ComputerPatchStatus;
import com.sysadminanywhere.common.inventory.model.HardwareComputerItem;
import com.sysadminanywhere.common.inventory.model.ComputerHardwareDetails;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.text.NumberFormat;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/hardware")
@Uses(Icon.class)
public class InventoryHardwareView extends Div implements HasDynamicTitle {

    private Grid<HardwareComputerItem> grid;
    private int patchStaleDays = 90;
    private TextField computerSearch;
    private final InventoryService inventoryService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    public InventoryHardwareView(InventoryService inventoryService, MessageSource messageSource, LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!inventoryService.ping()) {
            Notification notification = Notification.show(getMessage("common.error") + ": " + getMessage("inventory_hardware_view.service_unavailable"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            InventoryCoverage coverage = inventoryService.getInventoryCoverage();
            patchStaleDays = coverage.patchStaleDays() == null ? 90 : Math.max(1, coverage.patchStaleDays());
            computerSearch = new TextField(getMessage("inventory_hardware_view.computer_search"));
            computerSearch.setPlaceholder(getMessage("inventory_hardware_view.computer_search_placeholder"));
            computerSearch.setClearButtonVisible(true);
            computerSearch.setWidthFull();
            computerSearch.addValueChangeListener(event -> refreshGrid());
            VerticalLayout layout = new VerticalLayout(
                    new Span(getMessage("inventory_hardware_view.subtitle")),
                    computerSearch,
                    createGrid(),
                    createCoverageSection(coverage),
                    createSummarySection(getMessage("inventory_hardware_view.os_distribution"),
                            getMessage("inventory_hardware_view.os_distribution_hint"), createOperatingSystemSummary()),
                    createSummarySection(getMessage("inventory_hardware_view.patch_freshness"),
                            getMessage("inventory_hardware_view.patch_freshness_hint",
                                    new Object[]{patchStaleDays}), createPatchSummary()));
            layout.setSizeFull();
            layout.setPadding(true);
            layout.setSpacing(true);
            add(layout);
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String getMessage(String key, Object[] arguments) {
        return messageSource.getMessage(key, arguments, localeService.getCurrentLocale());
    }

    private Component createGrid() {
        grid = new Grid<>(HardwareComputerItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(HardwareComputerItem::name).setHeader(getMessage("inventory_hardware_view.computer")).setFlexGrow(1);
        grid.addColumn(item -> item.checkedAt() == null ? getMessage("inventory_hardware_view.scan_never") : item.checkedAt().toString())
                .setHeader(getMessage("inventory_hardware_view.last_scan")).setAutoWidth(true);
        grid.addColumn(item -> scanStatusLabel(item.scanStatus())).setHeader(getMessage("inventory_hardware_view.scan_status")).setAutoWidth(true);
        grid.addColumn(HardwareComputerItem::componentCount).setHeader(getMessage("inventory_hardware_view.component_count")).setAutoWidth(true);
        grid.addItemClickListener(event -> showComputerDetails(event.getItem()));
        grid.setPageSize(20);
        grid.setItems(query -> inventoryService.getHardwareComputers(
                PageRequest.of(query.getPage(), query.getPageSize()), computerSearch.getValue()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setHeight("420px");

        return grid;
    }

    private String scanStatusLabel(String status) {
        if (status == null || status.isBlank()) return getMessage("inventory_hardware_view.scan_unknown");
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "SUCCESS" -> getMessage("inventory_hardware_view.scan_success");
            case "ERROR", "FAILED" -> getMessage("inventory_hardware_view.scan_error");
            case "RUNNING" -> getMessage("inventory_hardware_view.scan_running");
            default -> status;
        };
    }

    private void showComputerDetails(HardwareComputerItem computer) {
        ComputerHardwareDetails details = inventoryService.getComputerHardwareDetails(computer.id());
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(computer.name());
        dialog.setWidth("min(900px, 95vw)");
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        if (details == null || details.components().isEmpty()) {
            content.add(new Span(getMessage("inventory_hardware_view.no_component_data")));
        } else {
            content.add(createComputerOverview(details));
            for (var component : details.components()) {
                Card card = new Card();
                card.setWidthFull();
                card.add(new H3(getHardwareTypeLabel(component.getType()) + " — " + component.getName()));
                Grid<com.sysadminanywhere.common.inventory.model.HardwarePropertyItem> properties = new Grid<>();
                properties.addColumn(item -> propertyLabel(item.getPropertyName())).setHeader(getMessage("inventory_hardware_view.property"));
                properties.addColumn(com.sysadminanywhere.common.inventory.model.HardwarePropertyItem::getPropertyValue)
                        .setHeader(getMessage("inventory_hardware_view.value")).setFlexGrow(1);
                properties.setItems(component.getProperties());
                properties.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
                properties.setAllRowsVisible(true);
                card.add(properties);
                content.add(card);
            }
        }
        dialog.add(content);
        dialog.open();
    }

    private Component createComputerOverview(ComputerHardwareDetails details) {
        var components = details.components();
        String model = join(findProperty(components, "ComputerSystem", "Manufacturer"),
                findProperty(components, "ComputerSystem", "Model"));
        String processor = firstNonBlank(findProperty(components, "Processor", "Name"),
                findProperty(components, "Processor", "Model"));
        String memory = findProperty(components, "ComputerSystem", "TotalPhysicalMemory");
        if (memory == null) memory = sumProperties(components, "PhysicalMemory", "Capacity");
        String storage = sumProperties(components, "DiskDrive", "Size");
        String os = firstNonBlank(findProperty(components, "OperatingSystem", "Caption"),
                findProperty(components, "OperatingSystem", "Name"));
        Div cards = new Div();
        cards.setWidthFull();
        cards.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(180px, 1fr))")
                .set("gap", "var(--lumo-space-s)");
        cards.add(overviewCard("model", model, "model_hint"),
                overviewCard("processor", processor, "processor_hint"),
                overviewCard("memory", formatBytes(memory), "memory_hint"),
                overviewCard("storage", formatBytes(storage), "storage_hint",
                        countComponents(components, "DiskDrive")),
                overviewCard("operating_system", os, "operating_system_hint"));
        return cards;
    }

    private Component overviewCard(String label, String value, String hint) {
        return overviewCard(label, value, hint, null);
    }

    private Component overviewCard(String label, String value, String hint, Integer argument) {
        Card card = new Card();
        card.setWidthFull();
        Span valueLabel = new Span(firstNonBlank(value, getMessage("computer_hardware_view.not_reported")));
        valueLabel.getStyle().set("font-weight", "600").set("font-size", "var(--lumo-font-size-m)");
        Span hintLabel = new Span(argument == null ? getMessage("computer_hardware_view." + hint)
                : getMessage("computer_hardware_view." + hint, new Object[]{argument}));
        hintLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        card.add(new Span(getMessage("computer_hardware_view." + label)), valueLabel, hintLabel);
        return card;
    }

    private int countComponents(java.util.List<com.sysadminanywhere.common.inventory.model.HardwareModelItem> components, String type) {
        return (int) components.stream().filter(component -> normalize(component.getType()).equals(normalize(type))).count();
    }

    private String findProperty(java.util.List<com.sysadminanywhere.common.inventory.model.HardwareModelItem> components,
                                String type, String propertyName) {
        return components.stream().filter(component -> normalize(component.getType()).equals(normalize(type)))
                .flatMap(component -> component.getProperties().stream())
                .filter(property -> propertyName.equalsIgnoreCase(property.getPropertyName()))
                .map(com.sysadminanywhere.common.inventory.model.HardwarePropertyItem::getPropertyValue)
                .filter(value -> value != null && !value.isBlank() && !"-".equals(value)).findFirst().orElse(null);
    }

    private String sumProperties(java.util.List<com.sysadminanywhere.common.inventory.model.HardwareModelItem> components,
                                 String type, String propertyName) {
        double total = components.stream().filter(component -> normalize(component.getType()).equals(normalize(type)))
                .flatMap(component -> component.getProperties().stream())
                .filter(property -> propertyName.equalsIgnoreCase(property.getPropertyName()))
                .map(com.sysadminanywhere.common.inventory.model.HardwarePropertyItem::getPropertyValue)
                .mapToDouble(this::parseNumber).sum();
        return total > 0 ? formatBytes(total) : null;
    }

    private String formatBytes(String value) {
        if (value == null) return null;
        double bytes = parseNumber(value);
        return bytes > 0 ? formatBytes(bytes) : value;
    }

    private String formatBytes(double bytes) {
        if (bytes <= 0) return null;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unit = 0;
        double size = bytes;
        while (size >= 1024 && unit < units.length - 1) { size /= 1024; unit++; }
        return NumberFormat.getNumberInstance(localeService.getCurrentLocale()).format(size)
                + " " + units[unit];
    }

    private double parseNumber(String value) {
        if (value == null) return 0;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
    }

    private String join(String first, String second) {
        return firstNonBlank(first, "") + (firstNonBlank(first, "").isBlank() || firstNonBlank(second, "").isBlank() ? "" : " ") + firstNonBlank(second, "");
    }

    private String firstNonBlank(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first;
    }

    private String propertyLabel(String name) {
        if (name == null || name.isBlank()) return "";
        return name.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
    }

    private Component createSummarySection(String title, String description, Component content) {
        Card card = new Card();
        card.setWidthFull();
        card.add(new H3(title), new Span(description), content);
        return card;
    }

    private Component createCoverageSection(InventoryCoverage coverage) {
        HorizontalLayout metrics = new HorizontalLayout(
                coverageMetric(getMessage("inventory_hardware_view.coverage_os_title"),
                        coverage.withOperatingSystem(), coverage.computers()),
                coverageMetric(getMessage("inventory_hardware_view.coverage_patch_title"),
                        coverage.withPatches(), coverage.computers()));
        metrics.setWidthFull();
        metrics.setFlexGrow(1, metrics.getComponentAt(0), metrics.getComponentAt(1));
        Card card = new Card();
        card.setWidthFull();
        card.add(new H3(getMessage("inventory_hardware_view.coverage")),
                new Span(getMessage("inventory_hardware_view.coverage_hint")), metrics);
        return card;
    }

    private Component coverageMetric(String label, long covered, long total) {
        VerticalLayout metric = new VerticalLayout();
        metric.setPadding(false);
        metric.setSpacing(false);
        metric.setWidthFull();
        Span name = new Span(label);
        name.getStyle().set("font-weight", "600");
        if (total <= 0) {
            metric.add(name, new Span(getMessage("inventory_hardware_view.coverage_no_computers")));
            return metric;
        }
        long missing = Math.max(0, total - covered);
        int percentage = (int) Math.round((covered * 100.0) / total);
        Span amount = new Span(getMessage("inventory_hardware_view.coverage_count", new Object[]{covered, total, percentage}));
        amount.getStyle().set("font-size", "var(--lumo-font-size-l)");
        ProgressBar progress = new ProgressBar();
        progress.setValue(Math.min(1.0, Math.max(0.0, covered / (double) total)));
        Span missingText = new Span(getMessage("inventory_hardware_view.coverage_missing", new Object[]{missing}));
        missingText.getStyle().set("color", missing == 0 ? "var(--lumo-success-text-color)" : "var(--lumo-secondary-text-color)");
        metric.add(name, amount, progress, missingText);
        return metric;
    }

    private String getHardwareTypeLabel(String type) {
        if (type == null) return "";
        String key = switch (type.replace(" ", "").toLowerCase(Locale.ROOT)) {
            case "computersystem" -> "computer_system";
            case "bios" -> "bios";
            case "baseboard" -> "base_board";
            case "diskdrive" -> "disk_drive";
            case "operatingsystem" -> "operating_system";
            case "processor" -> "processor";
            case "videocontroller" -> "video_controller";
            case "physicalmemory" -> "physical_memory";
            case "patch" -> "patch";
            default -> null;
        };
        return key == null ? type : getMessage("inventory_hardware_view." + key);
    }

    private Component createOperatingSystemSummary() {
        Grid<OperatingSystemCount> summary = new Grid<>();
        summary.setHeight("180px");
        summary.addColumn(OperatingSystemCount::name)
                .setHeader(getMessage("inventory_hardware_view.operating_system"))
                .setFlexGrow(1);
        summary.addColumn(OperatingSystemCount::computers)
                .setHeader(getMessage("inventory_hardware_view.computers_count"))
                .setAutoWidth(true);
        summary.setItems(inventoryService.getOperatingSystemCounts());
        summary.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        return summary;
    }

    private Component createPatchSummary() {
        Grid<ComputerPatchStatus> summary = new Grid<>();
        summary.setHeight("180px");
        summary.addColumn(ComputerPatchStatus::computer)
                .setHeader(getMessage("inventory_hardware_view.computer"));
        summary.addColumn(item -> item.lastPatchDate() == null || item.lastPatchDate().isBlank()
                        ? getMessage("inventory_hardware_view.patch_date_unavailable") : item.lastPatchDate())
                .setHeader(getMessage("inventory_hardware_view.last_patch"));
        summary.addColumn(item -> item.stale()
                        ? getMessage("inventory_hardware_view.needs_review", new Object[]{patchStaleDays})
                        : getMessage("inventory_hardware_view.within_freshness_limit", new Object[]{patchStaleDays}))
                .setHeader(getMessage("inventory_hardware_view.patch_status"));
        summary.setItems(inventoryService.getPatchStatuses());
        summary.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        return summary;
    }

    private void refreshGrid() {
        if (grid != null) grid.getDataProvider().refreshAll();
    }

    public String getPageTitle() {
        return getMessage("inventory_hardware_view.title");
    }

}
