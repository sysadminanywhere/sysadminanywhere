package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerHardwareDetails;
import com.sysadminanywhere.common.inventory.model.HardwareChangeItem;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/hardware/:modelId/computers/:computerId")
public class InventoryComputerHardwareView extends Div implements BeforeEnterObserver, HasDynamicTitle {
    private final InventoryService inventoryService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final InventoryHardwareExplorer explorer;
    private Long modelId;
    private Long computerId;
    private ComputerHardwareDetails details;

    public InventoryComputerHardwareView(InventoryService inventoryService, MessageSource messageSource,
                                         LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        this.explorer = new InventoryHardwareExplorer(messageSource, localeService);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        modelId = event.getRouteParameters().get("modelId").map(Long::valueOf).orElse(null);
        computerId = event.getRouteParameters().get("computerId").map(Long::valueOf).orElse(null);
        render();
    }

    private void render() {
        removeAll();
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);
        add(content);
        if (modelId == null || computerId == null) {
            content.add(new Span(msg("inventory_hardware_view.no_component_data")));
            return;
        }

        details = inventoryService.getComputerHardwareDetails(computerId);
        if (details == null || details.computer() == null || details.components() == null) {
            content.add(new Span(msg("inventory_hardware_view.no_component_data")));
            return;
        }
        content.add(new Span(msg("inventory_hardware_view.last_scan") + ": "
                + (details.computer().checkedAt() == null
                ? msg("inventory_hardware_view.scan_never") : date(details.computer().checkedAt()))));
        if (details.components().isEmpty()) {
            content.add(new Span(msg("inventory_hardware_view.no_component_data")));
        } else {
            content.add(explorer.createExplorer(details.components()));
        }
        content.add(new H3(msg("inventory_hardware_view.configuration_history")), createHistoryGrid());
    }

    private Grid<HardwareChangeItem> createHistoryGrid() {
        Grid<HardwareChangeItem> grid = new Grid<>(HardwareChangeItem.class, false);
        grid.addColumn(item -> date(item.changedAt()))
                .setHeader(msg("inventory_hardware_view.change_date")).setAutoWidth(true);
        grid.addColumn(item -> explorer.typeLabel(item.changeType()))
                .setHeader(msg("inventory_hardware_view.change_type")).setAutoWidth(true);
        grid.addColumn(HardwareChangeItem::hardwareName)
                .setHeader(msg("inventory_hardware_view.hardware_name_header")).setFlexGrow(1);
        grid.addColumn(item -> item.propertyName() == null ? "—" : explorer.propertyLabel(item.propertyName()))
                .setHeader(msg("inventory_hardware_view.property")).setAutoWidth(true);
        grid.addColumn(item -> display(item.oldValue()))
                .setHeader(msg("inventory_hardware_view.previous_value")).setFlexGrow(1);
        grid.addColumn(item -> display(item.newValue()))
                .setHeader(msg("inventory_hardware_view.current_value")).setFlexGrow(1);
        grid.setPageSize(20);
        grid.setItems(query -> inventoryService.getHardwareChanges(computerId, null,
                PageRequest.of(query.getPage(), query.getPageSize())).stream());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setWidthFull();
        grid.setHeight("300px");
        return grid;
    }

    private String date(LocalDateTime value) {
        return value == null ? "—" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return details == null || details.computer() == null
                ? msg("inventory_hardware_view.title") : details.computer().name();
    }
}
