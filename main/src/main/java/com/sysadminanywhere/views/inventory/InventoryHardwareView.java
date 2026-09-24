package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareItem;
import com.sysadminanywhere.common.inventory.model.OperatingSystemCount;
import com.sysadminanywhere.common.inventory.model.InventoryCoverage;
import com.sysadminanywhere.common.inventory.model.ComputerPatchStatus;
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
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/hardware")
@Uses(Icon.class)
public class InventoryHardwareView extends Div implements HasDynamicTitle {

    private Grid<HardwareItem> grid;
    private int patchStaleDays = 90;

    private Filters filters;
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
            filters = new Filters(() -> refreshGrid(), messageSource, localeService);
            InventoryCoverage coverage = inventoryService.getInventoryCoverage();
            patchStaleDays = coverage.patchStaleDays() == null ? 90 : Math.max(1, coverage.patchStaleDays());
            VerticalLayout layout = new VerticalLayout(
                    new Span(getMessage("inventory_hardware_view.subtitle")),
                    createFilterSection(),
                    createCoverageSection(coverage),
                    createSummarySection(getMessage("inventory_hardware_view.os_distribution"),
                            getMessage("inventory_hardware_view.os_distribution_hint"), createOperatingSystemSummary()),
                    createSummarySection(getMessage("inventory_hardware_view.patch_freshness"),
                            getMessage("inventory_hardware_view.patch_freshness_hint",
                                    new Object[]{patchStaleDays}), createPatchSummary()),
                    createSummarySection(getMessage("inventory_hardware_view.hardware_records"),
                            getMessage("inventory_hardware_view.hardware_records_hint"), createGrid()));
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

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span(getMessage("common.filters"));
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div {

        private final ComboBox<String> hardwareType;
        private final TextField name;
        private final MessageSource messageSource;
        private final LocaleService localeService;
        private final Map<String, String> translationToEnglishMap;

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.hardwareType = new ComboBox<>(getMessage("inventory_hardware_view.filter_category"));
            this.name = new TextField(getMessage("inventory_hardware_view.filter_name"));
            this.name.setPlaceholder(getMessage("inventory_hardware_view.filter_name_placeholder"));

            // Create reverse mapping from translated values to English keys
            this.translationToEnglishMap = new java.util.HashMap<>();
            translationToEnglishMap.put(getMessage("inventory_hardware_view.computer_system"), "Computer System");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.bios"), "BIOS");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.base_board"), "Base Board");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.disk_drive"), "Disk Drive");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.operating_system"), "Operating System");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.processor"), "Processor");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.video_controller"), "Video Controller");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.physical_memory"), "Physical Memory");
            translationToEnglishMap.put(getMessage("inventory_hardware_view.patch"), "Patch");

            hardwareType.setItems(getMessage("inventory_hardware_view.computer_system"), getMessage("inventory_hardware_view.bios"), getMessage("inventory_hardware_view.base_board"), getMessage("inventory_hardware_view.disk_drive"), getMessage("inventory_hardware_view.operating_system"), getMessage("inventory_hardware_view.processor"), getMessage("inventory_hardware_view.video_controller"), getMessage("inventory_hardware_view.physical_memory"), getMessage("inventory_hardware_view.patch"));
            hardwareType.setValue(getMessage("inventory_hardware_view.computer_system"));

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            // Action buttons
            Button resetBtn = new Button(getMessage("common.reset"));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();

                hardwareType.clear();
                hardwareType.setValue(getMessage("inventory_hardware_view.computer_system"));

                onSearch.run();
            });
            Button searchBtn = new Button(getMessage("common.search"));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(hardwareType, name, actions);
        }

        private String getMessage(String key) {
            return messageSource.getMessage(key, null, localeService.getCurrentLocale());
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            filters.put("name", name.getValue());

            String selectedValue = hardwareType.getValue();
            String englishValue = translationToEnglishMap.getOrDefault(selectedValue, selectedValue);
            filters.put("type", englishValue.replace(" ", ""));
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(HardwareItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(HardwareItem::getName).setHeader(getMessage("inventory_hardware_view.hardware_name_header")).setAutoWidth(true);
        grid.addColumn(item -> getHardwareTypeLabel(item.getType()))
                .setHeader(getMessage("inventory_hardware_view.hardware_type_header")).setAutoWidth(true);

        grid.addItemClickListener(item -> {
            grid.getUI().ifPresent(ui ->
                    ui.navigate("inventory/hardware/" + item.getItem().getId() + "/details"));
        });

        grid.setItems(query -> inventoryService.getHardware(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters.getFilters()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private Component createFilterSection() {
        VerticalLayout section = new VerticalLayout(createMobileFilters(),
                new Span(getMessage("inventory_hardware_view.filters_hint")), filters);
        section.setPadding(false);
        section.setSpacing(false);
        section.setWidthFull();
        return section;
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
        grid.getDataProvider().refreshAll();
    }

    public String getPageTitle() {
        return getMessage("inventory_hardware_view.title");
    }

}
