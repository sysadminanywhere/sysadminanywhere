package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.HardwareEntity;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.service.ComputersService;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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
                    createTable(event.getValue(), convert(computersService.getBaseBoard(id)));
                    break;
                case "Disk drive":
                    createTabs(event.getValue(), convertList(computersService.getDiskDrive(id)));
                    break;
                case "Operating system":
                    createTable(event.getValue(), convert(computersService.getOperatingSystem(id)));
                    break;
                case "Disk partition":
                    createTabs(event.getValue(), convertList(computersService.getDiskPartition(id)));
                    break;
                case "Processor":
                    createTabs(event.getValue(), convertList(computersService.getProcessor(id)));
                    break;
                case "Video controller":
                    createTabs(event.getValue(), convertList(computersService.getVideoController(id)));
                    break;
                case "Physical memory":
                    createTabs(event.getValue(), convertList(computersService.getPhysicalMemory(id)));
                    break;
                case "Logical disk":
                    createTabs(event.getValue(), convertList(computersService.getLogicalDisk(id)));
                    break;
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);

        divTable.getStyle().setMarginLeft("25px");

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
                String valueStr = "-";

                if (value instanceof String[]) {
                    String[] array = (String[]) value;
                    valueStr = Arrays.stream(array)
                            .collect(Collectors.joining(", "));
                } else {
                    valueStr = value != null ? value.toString() : "-";
                }

                result.add(new HardwareEntity(name, valueStr));
            } catch (IllegalAccessException e) {
            }
        }

        return result;
    }

    private List<List<HardwareEntity>> convertList(Object obj) {
        List<List<HardwareEntity>> result = new ArrayList<>();

        if (obj == null) return result;

        if (obj instanceof List) {
            List<Object> list = (List) obj;
            for (Object o : list) {
                result.add(convert(o));
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

    private void createTabs(String name, List<List<HardwareEntity>> list) {
        TabSheet tabSheet = new TabSheet();
        H4 title = new H4(name);
        title.getStyle().setMarginTop("10px");

        Integer n = 0;

        for (List<HardwareEntity> lst : list) {
            Table table = new Table("");

            for (HardwareEntity entity : lst) {
                table.add(pascalToSpaced(entity.getName()), entity.getValue());
            }

            tabSheet.add(n.toString(), table);
            n++;
        }

        divTable.add(title, tabSheet);
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