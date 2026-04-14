package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareItem;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@PageTitle("inventory_hardware_view.title")
@Route(value = "inventory/hardware")
@Uses(Icon.class)
public class InventoryHardwareView extends Div {

    private Grid<HardwareItem> grid;

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
            Notification notification = Notification.show(getMessage("common.error") + ": Inventory service is unavailable!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            filters = new Filters(() -> refreshGrid(), messageSource, localeService);
            VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(false);
            add(layout);
        }
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
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

        public Filters(Runnable onSearch, MessageSource messageSource, LocaleService localeService) {
            this.messageSource = messageSource;
            this.localeService = localeService;

            this.hardwareType = new ComboBox<>("Type");
            this.name = new TextField("Name");

            hardwareType.setItems("Computer System", "BIOS", "Base Board", "Disk Drive", "Operating System", "Processor", "Video Controller", "Physical Memory");
            hardwareType.setValue("Computer System");

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
                hardwareType.setValue("Computer System");

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
            filters.put("type", hardwareType.getValue().replace(" ", ""));
            return filters;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(HardwareItem.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("type").setAutoWidth(true);

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

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
