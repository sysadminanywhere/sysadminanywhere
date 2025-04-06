package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.model.wmi.ComputerSystemEntity;
import com.sysadminanywhere.model.wmi.HardwareEntity;
import com.sysadminanywhere.model.wmi.SoftwareEntity;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Hardware")
@Route(value = "management/computers/:id?/hardware")
@PermitAll
@Uses(Icon.class)
public class ComputerHardwareView extends Div implements BeforeEnterObserver {

    private String id;

    private final ComputersService computersService;
    Div divTable = new Div();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerHardwareView(ComputersService computersService) {
        this.computersService = computersService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Computer system", "BIOS", "Base board", "Disk drive", "Operating system", "Disk partition", "Processor", "Video controller", "Physical memory", "Logical disk");

        listBox.addValueChangeListener(event -> {
            divTable.removeAll();

            switch (event.getValue()) {
                case "Computer system":
                    createTable(event.getValue(), convert(computersService.getComputerSystem(id)));
                    break;
                case "BIOS":
                    createTable(event.getValue(), convert(computersService.getBIOS(id)));
                    break;
                case "Base board":
                    break;
                case "Disk drive":
                    break;
                case "Operating system":
                    break;
                case "Disk partition":
                    break;
                case "Processor":
                    break;
                case "Video controller":
                    break;
                case "Physical memory":
                    break;
                case "Logical disk":
                    break;
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        divTable.getStyle().setMarginLeft("50px");

        layout.add(listBox, divTable);

        add(layout);
    }

    private List<HardwareEntity> convert(Object obj) {
        List<HardwareEntity> result = new ArrayList<>();

        if (obj == null) return result;

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                String name = field.getName();
                Object value = field.get(obj);
                String valueStr = value != null ? value.toString() : "-";

                result.add(new HardwareEntity(name, valueStr));
            } catch (IllegalAccessException e) {
            }
        }

        return result;
    }

    private void createTable(String name, List<HardwareEntity> items) {
        Table table = new Table(name);

        for (HardwareEntity entity : items) {
            table.add(pascalToSpaced(entity.getName()), entity.getValue());
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