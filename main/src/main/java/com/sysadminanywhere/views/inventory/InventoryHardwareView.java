package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareCatalogItem;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.util.Locale;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/hardware")
@Uses(Icon.class)
public class InventoryHardwareView extends Div implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private Grid<HardwareCatalogItem> grid;
    private Filters filters;

    public InventoryHardwareView(InventoryService inventoryService, MessageSource messageSource,
                                 LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassName("gridwith-filters-view");

        if (!inventoryService.ping()) {
            Notification notification = Notification.show(
                    getMessage("common.error") + ": " + getMessage("inventory_hardware_view.service_unavailable"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        filters = new Filters(this::refreshGrid);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span(getMessage("common.filters"));
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(event -> {
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

    private Component createGrid() {
        grid = new Grid<>(HardwareCatalogItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(HardwareCatalogItem::name)
                .setHeader(getMessage("inventory_hardware_view.hardware_name_header")).setAutoWidth(true);
        grid.addColumn(item -> getHardwareTypeLabel(item.type()))
                .setHeader(getMessage("inventory_hardware_view.hardware_type_header")).setAutoWidth(true);
        grid.addColumn(HardwareCatalogItem::computerCount)
                .setHeader(getMessage("inventory_hardware_view.installed_on_computers")).setAutoWidth(true);

        grid.addItemClickListener(event -> grid.getUI().ifPresent(ui ->
                ui.navigate("inventory/hardware/" + event.getItem().id() + "/details")));
        grid.setItems(query -> inventoryService.getHardwareCatalog(
                PageRequest.of(query.getPage(), query.getPageSize()),
                filters.name.getValue(), filters.category.getValue()).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        return grid;
    }

    private void refreshGrid() {
        if (grid != null) grid.getDataProvider().refreshAll();
    }

    private String getHardwareTypeLabel(String type) {
        if (type == null) return "";
        String key = switch (type.replace(" ", "").toLowerCase(Locale.ROOT)) {
            case "computersystem" -> "computer_system";
            case "bios" -> "bios";
            case "baseboard" -> "base_board";
            case "diskdrive" -> "disk_drive";
            case "processor" -> "processor";
            case "videocontroller" -> "video_controller";
            case "physicalmemory" -> "physical_memory";
            default -> null;
        };
        return key == null ? type : getMessage("inventory_hardware_view." + key);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return getMessage("inventory_hardware_view.title");
    }

    private class Filters extends Div {
        private final TextField name = new TextField(getMessage("inventory_hardware_view.filter_name"));
        private final ComboBox<String> category = new ComboBox<>(getMessage("inventory_hardware_view.filter_category"));

        private Filters(Runnable onSearch) {
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            name.setPlaceholder(getMessage("inventory_hardware_view.filter_name_placeholder"));
            name.setClearButtonVisible(true);
            category.setItems("Processor", "VideoController", "DiskDrive", "PhysicalMemory",
                    "ComputerSystem", "BaseBoard", "BIOS");
            category.setPlaceholder(getMessage("inventory_hardware_view.all_hardware_types"));
            category.setItemLabelGenerator(InventoryHardwareView.this::getHardwareTypeLabel);
            category.setClearButtonVisible(true);

            Button reset = new Button(getMessage("common.reset"));
            reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            reset.addClickListener(event -> {
                name.clear();
                category.clear();
                onSearch.run();
            });
            Button search = new Button(getMessage("common.search"));
            search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            search.addClickListener(event -> onSearch.run());

            Div actions = new Div(reset, search);
            actions.addClassNames(LumoUtility.Gap.SMALL, "actions");
            add(name, category, actions);
        }
    }
}
