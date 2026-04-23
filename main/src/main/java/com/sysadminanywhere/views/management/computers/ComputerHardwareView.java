package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.model.wmi.HardwareEntity;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import org.springframework.context.MessageSource;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RolesAllowed("ADMIN")
@Route(value = "management/computers/:id?/hardware")
@Uses(Icon.class)
public class ComputerHardwareView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private String id;

    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    Div divTable = new Div();

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").
                orElse(null);
    }

    public ComputerHardwareView(ComputersService computersService, MessageSource messageSource, LocaleService localeService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        addClassNames("gridwith-filters-view");

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems(
            getMessage("computer_hardware_view.computer_system"),
            getMessage("computer_hardware_view.bios"),
            getMessage("computer_hardware_view.base_board"),
            getMessage("computer_hardware_view.disk_drive"),
            getMessage("computer_hardware_view.operating_system"),
            getMessage("computer_hardware_view.disk_partition"),
            getMessage("computer_hardware_view.processor"),
            getMessage("computer_hardware_view.video_controller"),
            getMessage("computer_hardware_view.physical_memory"),
            getMessage("computer_hardware_view.logical_disk")
        );

        listBox.addValueChangeListener(event -> {
            divTable.removeAll();

            String value = event.getValue();

            if (value.equals(getMessage("computer_hardware_view.computer_system"))) {
                createTable(value, convert(computersService.getComputerSystem(id)));
            } else if (value.equals(getMessage("computer_hardware_view.bios"))) {
                createTable(value, convert(computersService.getBIOS(id)));
            } else if (value.equals(getMessage("computer_hardware_view.base_board"))) {
                createTable(value, convert(computersService.getBaseBoard(id)));
            } else if (value.equals(getMessage("computer_hardware_view.disk_drive"))) {
                createTabs(value, convertList(computersService.getDiskDrive(id)));
            } else if (value.equals(getMessage("computer_hardware_view.operating_system"))) {
                createTable(value, convert(computersService.getOperatingSystem(id)));
            } else if (value.equals(getMessage("computer_hardware_view.disk_partition"))) {
                createTabs(value, convertList(computersService.getDiskPartition(id)));
            } else if (value.equals(getMessage("computer_hardware_view.processor"))) {
                createTabs(value, convertList(computersService.getProcessor(id)));
            } else if (value.equals(getMessage("computer_hardware_view.video_controller"))) {
                createTabs(value, convertList(computersService.getVideoController(id)));
            } else if (value.equals(getMessage("computer_hardware_view.physical_memory"))) {
                createTabs(value, convertList(computersService.getPhysicalMemory(id)));
            } else if (value.equals(getMessage("computer_hardware_view.logical_disk"))) {
                createTabs(value, convertList(computersService.getLogicalDisk(id)));
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        divTable.getStyle().setMarginLeft("25px");

        layout.add(listBox, divTable);

        add(layout);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
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

                if (value instanceof String[] array) {
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

    public String getPageTitle() {
        return getMessage("computer_hardware_view.title");
    }

}
