package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareChangeItem;
import com.sysadminanywhere.common.inventory.model.HardwareComputerItem;
import com.sysadminanywhere.common.inventory.model.HardwareCatalogItem;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/hardware/:id?/details")
public class InventoryHardwarePropertiesView extends Div implements BeforeEnterObserver, HasDynamicTitle {
    private final InventoryService inventoryService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private Long modelId;
    private HardwareCatalogItem model;

    public InventoryHardwarePropertiesView(InventoryService inventoryService, MessageSource messageSource,
                                           LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        modelId = event.getRouteParameters().get("id").map(Long::valueOf).orElse(null);
        render();
    }

    private void render() {
        removeAll();
        if (modelId == null) return;
        model = inventoryService.getHardwareCatalogItem(modelId);
        if (model == null) {
            add(new Span(msg("inventory_hardware_view.no_component_data")));
            return;
        }
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        Div header = new Div(new H2(model.name()), new Span(typeLabel(model.type()) + " · "
                + msg("inventory_hardware_view.installed_on_computers") + ": " + model.computerCount()));
        header.addClassName("hardware-catalog-header");
        content.add(header, new H3(msg("inventory_hardware_view.computers_with_model")), createComputersGrid(),
                new H3(msg("inventory_hardware_view.configuration_history")), createHistoryGrid(null, modelId));
        add(content);
    }

    private Grid<HardwareComputerItem> createComputersGrid() {
        Grid<HardwareComputerItem> grid = new Grid<>(HardwareComputerItem.class, false);
        grid.addColumn(HardwareComputerItem::name).setHeader(msg("inventory_hardware_view.computer")).setFlexGrow(1);
        grid.addColumn(item -> item.checkedAt() == null ? msg("inventory_hardware_view.scan_never") : date(item.checkedAt()))
                .setHeader(msg("inventory_hardware_view.last_scan")).setAutoWidth(true);
        grid.addColumn(HardwareComputerItem::componentCount)
                .setHeader(msg("inventory_hardware_view.component_count")).setAutoWidth(true);
        grid.addItemClickListener(event -> grid.getUI().ifPresent(ui -> ui.navigate(
                "inventory/hardware/" + modelId + "/computers/" + event.getItem().id())));
        grid.setPageSize(20);
        grid.setItems(query -> inventoryService.getComputersByHardwareModel(modelId,
                PageRequest.of(query.getPage(), query.getPageSize())).stream());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setHeight("320px");
        return grid;
    }

    private Grid<HardwareChangeItem> createHistoryGrid(Long computerId, Long selectedModelId) {
        Grid<HardwareChangeItem> grid = new Grid<>(HardwareChangeItem.class, false);
        grid.addColumn(item -> date(item.changedAt())).setHeader(msg("inventory_hardware_view.change_date")).setAutoWidth(true);
        grid.addColumn(item -> typeLabel(item.changeType())).setHeader(msg("inventory_hardware_view.change_type")).setAutoWidth(true);
        grid.addColumn(HardwareChangeItem::computerName).setHeader(msg("inventory_hardware_view.computer")).setAutoWidth(true);
        grid.addColumn(HardwareChangeItem::hardwareName).setHeader(msg("inventory_hardware_view.hardware_name_header")).setFlexGrow(1);
        grid.addColumn(item -> item.propertyName() == null ? "—" : propertyLabel(item.propertyName()))
                .setHeader(msg("inventory_hardware_view.property")).setAutoWidth(true);
        grid.addColumn(item -> display(item.oldValue())).setHeader(msg("inventory_hardware_view.previous_value")).setFlexGrow(1);
        grid.addColumn(item -> display(item.newValue())).setHeader(msg("inventory_hardware_view.current_value")).setFlexGrow(1);
        grid.setPageSize(20);
        grid.setItems(query -> inventoryService.getHardwareChanges(computerId, selectedModelId,
                PageRequest.of(query.getPage(), query.getPageSize())).stream());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setHeight("300px");
        return grid;
    }

    private String typeLabel(String type) {
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
            case "opticaldrive" -> "optical_drive";
            case "sounddevice" -> "audio";
            case "keyboard" -> "keyboard";
            case "pointingdevice" -> "pointing_device";
            case "networkadapter" -> "network";
            case "patch" -> "patch";
            case "installed" -> "change_installed";
            case "first_observed" -> "change_first_observed";
            case "removed" -> "change_removed";
            case "replaced" -> "change_replaced";
            default -> null;
        };
        return key == null ? type : msg("inventory_hardware_view." + key);
    }

    private String propertyLabel(String value) {
        if (value == null) return "";
        String fallback = value.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
        String key = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        return messageSource.getMessage("computer_hardware_view.property." + key, null, fallback,
                localeService.getCurrentLocale());
    }

    private String date(java.time.LocalDateTime value) {
        return value == null ? "—" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String msg(String key) { return messageSource.getMessage(key, null, localeService.getCurrentLocale()); }
    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }
    @Override public String getPageTitle() { return model == null ? msg("inventory_hardware_view.title") : model.name(); }
}
