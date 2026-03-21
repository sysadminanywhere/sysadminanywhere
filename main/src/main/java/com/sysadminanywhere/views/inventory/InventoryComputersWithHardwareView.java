package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.service.InventoryService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

@RolesAllowed("ADMIN")
@PageTitle("Hardware inventory")
@Route(value = "inventory/hardware/:id?/computer")
@Uses(Icon.class)
public class InventoryComputersWithHardwareView extends Div implements BeforeEnterObserver {

    private Long id = 0L;

    private Grid<ComputerItem> grid;

    private Filters filters;
    private final InventoryService inventoryService;

    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").
                orElse(null);

        if(idParam != null && !idParam.isEmpty())
            id = Long.valueOf(idParam);
    }

    public InventoryComputersWithHardwareView(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        if (!inventoryService.ping()) {
            com.vaadin.flow.component.notification.Notification notification = 
                com.vaadin.flow.component.notification.Notification.show("Inventory service is unavailable!");
            notification.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
        } else {
            filters = new Filters(() -> refreshGrid());
            grid = createGrid();
            VerticalLayout layout = new VerticalLayout(filters, grid);
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(false);
            add(layout);
        }
    }

    private Grid<ComputerItem> createGrid() {
        Grid<ComputerItem> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        grid.addColumn("name").setHeader("Computer Name").setAutoWidth(true);
        grid.addColumn("checkingDate").setHeader("Last Check").setAutoWidth(true);

        grid.addComponentColumn(computer -> {
            Button button = new Button("View Hardware");
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            button.addClickListener(e -> {
                button.getUI().ifPresent(ui -> 
                    ui.navigate("management/computers/" + computer.getId() + "/hardware"));
            });
            return button;
        }).setHeader("Actions").setAutoWidth(true);

        // Create simple data provider
        grid.setItems(inventoryService.getComputersWithHardware(id, Pageable.unpaged(), Map.of()).getContent());
        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private static class Filters extends Div {
        private final TextField name;

        public Filters(Runnable onSearch) {
            addClassNames("filter-layout");
            setWidthFull();

            name = new TextField("Computer Name");
            name.setPlaceholder("Filter by computer name...");
            name.setClearButtonVisible(true);
            name.setWidth("200px");
            name.addValueChangeListener(e -> onSearch.run());

            HorizontalLayout layout = new HorizontalLayout(name);
            layout.setPadding(true);
            layout.setSpacing(true);
            add(layout);
        }

        public Map<String, String> getFilters() {
            Map<String, String> filters = new HashMap<>();
            String nameValue = name.getValue();
            if (nameValue != null && !nameValue.isEmpty()) {
                filters.put("name", nameValue);
            }
            return filters;
        }
    }
}
