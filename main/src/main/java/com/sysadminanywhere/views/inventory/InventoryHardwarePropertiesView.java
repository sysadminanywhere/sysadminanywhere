package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareModelItem;
import com.sysadminanywhere.common.inventory.model.HardwarePropertyItem;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.service.InventoryService;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@RolesAllowed("ADMIN")
@PageTitle("Hardware details")
@Route(value = "inventory/hardware/:id?/details")
@Uses(Icon.class)
public class InventoryHardwarePropertiesView extends Div  implements BeforeEnterObserver {

    private final InventoryService inventoryService;

    private Long id = 0L;
    Div divTable = new Div();

    public InventoryHardwarePropertiesView(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        divTable.getStyle().setMarginLeft("25px");
        add(divTable);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idParam = event.getRouteParameters().get("id").
                orElse(null);

        if(idParam != null && !idParam.isEmpty())
            id = Long.valueOf(idParam);

        updateView();
    }

    private void updateView() {
        if (id != null) {
            HardwareModelItem hardwareModelItem = inventoryService.getHardwareModelProperties(id);
            setTitle(hardwareModelItem.getType());
            divTable.removeAll();
            createTable(hardwareModelItem.getName(), hardwareModelItem.getProperties());
        }
    }

    private void createTable(String name, List<HardwarePropertyItem> items) {
        Table table = new Table(name);

        for (HardwarePropertyItem item : items) {
            table.add(pascalToSpaced(item.getPropertyName()), item.getPropertyValue());
        }

        divTable.add(table);
    }

    public String pascalToSpaced(String input) {
        if (input == null || input.isEmpty()) return input;

        input = capitalizeFirstLetter(input);

        String withSpaces = input
                .replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        return  withSpaces.trim();
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}