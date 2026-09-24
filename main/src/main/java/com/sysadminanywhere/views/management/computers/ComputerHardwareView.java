package com.sysadminanywhere.views.management.computers;

import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.model.wmi.*;
import com.sysadminanywhere.service.ComputersService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.details.Details;
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

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RolesAllowed("ADMIN")
@Route(value = "management/computers/:id?/hardware")
public class ComputerHardwareView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private String id;

    private final ComputersService computersService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final VerticalLayout content = new VerticalLayout();

    public ComputerHardwareView(ComputersService computersService, MessageSource messageSource,
                                LocaleService localeService) {
        this.computersService = computersService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);
        add(content);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        id = event.getRouteParameters().get("id").orElse(null);
        updateView();
    }

    private void updateView() {
        content.removeAll();
        if (id == null || id.isBlank()) {
            content.add(new Span(message("computer_hardware_view.no_computer")));
            return;
        }

        ComputerSystemEntity system = computersService.getComputerSystem(id);
        OperatingSystemEntity operatingSystem = computersService.getOperatingSystem(id);
        List<ProcessorEntity> processors = safeList(computersService.getProcessor(id));
        List<PhysicalMemoryEntity> memory = safeList(computersService.getPhysicalMemory(id));
        List<DiskDriveEntity> disks = safeList(computersService.getDiskDrive(id));

        content.add(new Span(message("computer_hardware_view.subtitle")), createOverview(system, operatingSystem,
                processors, memory, disks));

        VerticalLayout components = new VerticalLayout();
        components.setWidthFull();
        components.setPadding(false);
        components.setSpacing(true);
        components.add(new H3(message("computer_hardware_view.components")));
        components.add(createHardwareObjectSection("computer_system", system));
        components.add(createHardwareObjectSection("operating_system", operatingSystem));
        components.add(createHardwareListSection("processor", processors));
        components.add(createHardwareListSection("physical_memory", memory));
        components.add(createHardwareListSection("disk_drive", disks));
        components.add(createHardwareListSection("video_controller", safeList(computersService.getVideoController(id))));
        components.add(createHardwareObjectSection("base_board", computersService.getBaseBoard(id)));
        components.add(createHardwareObjectSection("bios", computersService.getBIOS(id)));
        components.add(createHardwareListSection("disk_partition", safeList(computersService.getDiskPartition(id))));
        components.add(createHardwareListSection("logical_disk", safeList(computersService.getLogicalDisk(id))));
        content.add(components);
    }

    private Component createOverview(ComputerSystemEntity system, OperatingSystemEntity os,
                                     List<ProcessorEntity> processors, List<PhysicalMemoryEntity> memory,
                                     List<DiskDriveEntity> disks) {
        Div overview = new Div();
        overview.setWidthFull();
        overview.getStyle().set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))")
                .set("gap", "var(--lumo-space-m)");
        overview.add(
                summaryCard(message("computer_hardware_view.model"), join(system == null ? null : system.getManufacturer(),
                        system == null ? null : system.getModel()), message("computer_hardware_view.model_hint")),
                summaryCard(message("computer_hardware_view.processor"), processorSummary(processors),
                        processorHint(processors)),
                summaryCard(message("computer_hardware_view.memory"), memorySummary(system, memory),
                        message("computer_hardware_view.memory_hint", memory.size())),
                summaryCard(message("computer_hardware_view.storage"), storageSummary(disks),
                        message("computer_hardware_view.storage_hint", disks.size())),
                summaryCard(message("computer_hardware_view.operating_system"), osSummary(os),
                        message("computer_hardware_view.operating_system_hint")));
        return overview;
    }

    private Card summaryCard(String title, String value, String hint) {
        Card card = new Card();
        card.setWidthFull();
        Span valueText = new Span(value == null || value.isBlank() ? message("computer_hardware_view.not_reported") : value);
        valueText.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "600");
        Span hintText = new Span(hint);
        hintText.getStyle().set("color", "var(--lumo-secondary-text-color)");
        card.add(new Span(title), valueText, hintText);
        return card;
    }

    private String processorSummary(List<ProcessorEntity> processors) {
        if (processors.isEmpty()) return message("computer_hardware_view.not_reported");
        return firstNonBlank(processors.get(0).getName(), processors.get(0).getModel());
    }

    private String processorHint(List<ProcessorEntity> processors) {
        if (processors.isEmpty()) return message("computer_hardware_view.no_component_data");
        long cores = processors.stream().mapToLong(item -> parseLong(item.getNumberOfCores())).sum();
        long threads = processors.stream().mapToLong(item -> parseLong(item.getNumberOfLogicalProcessors())).sum();
        if (cores == 0 && threads == 0) return message("computer_hardware_view.processor_count", processors.size());
        return message("computer_hardware_view.core_thread_count", cores, threads);
    }

    private String memorySummary(ComputerSystemEntity system, List<PhysicalMemoryEntity> memory) {
        double bytes = system == null ? 0 : parseDouble(system.getTotalPhysicalMemory());
        if (bytes == 0) bytes = memory.stream().mapToDouble(PhysicalMemoryEntity::getCapacity).sum();
        return bytes > 0 ? formatBytes(bytes) : message("computer_hardware_view.not_reported");
    }

    private String storageSummary(List<DiskDriveEntity> disks) {
        double bytes = disks.stream().mapToDouble(item -> parseDouble(item.getSize())).sum();
        return bytes > 0 ? formatBytes(bytes) : message("computer_hardware_view.not_reported");
    }

    private String osSummary(OperatingSystemEntity os) {
        if (os == null) return message("computer_hardware_view.not_reported");
        return join(os.getCaption(), os.getVersion());
    }

    private Table createTable(List<HardwareEntity> properties) {
        Table table = new Table("", "220px");
        for (HardwareEntity item : properties) {
            table.add(propertyLabel(item.getName()), formatProperty(item.getName(), item.getValue()));
        }
        return table;
    }

    private String componentName(String type, List<HardwareEntity> properties, int index) {
        String[] preferred = switch (type) {
            case "processor" -> new String[]{"name", "model"};
            case "physical_memory" -> new String[]{"deviceLocator", "partNumber", "manufacturer"};
            case "disk_drive" -> new String[]{"model", "name"};
            case "video_controller" -> new String[]{"name", "caption"};
            case "disk_partition", "logical_disk" -> new String[]{"name", "deviceID"};
            default -> new String[]{"name", "model"};
        };
        for (String key : preferred) {
            String value = properties.stream().filter(item -> key.equals(item.getName()))
                    .map(HardwareEntity::getValue).filter(item -> item != null && !item.isBlank() && !"-".equals(item))
                    .findFirst().orElse(null);
            if (value != null) return value;
        }
        return message("computer_hardware_view.component_number", message("computer_hardware_view." + type), index);
    }

    private Set<String> importantPropertiesFor(String type) {
        return switch (type) {
            case "computer_system" -> Set.of("manufacturer", "model", "totalPhysicalMemory", "numberOfProcessors",
                    "numberOfLogicalProcessors", "systemType", "domain", "workgroup", "hypervisorPresent");
            case "operating_system" -> Set.of("caption", "version", "osArchitecture", "cSDVersion", "manufacturer", "locale");
            case "processor" -> Set.of("name", "manufacturer", "numberOfCores", "numberOfLogicalProcessors", "maxClockSpeed", "currentClockSpeed", "socketDesignation", "virtualizationFirmwareEnabled");
            case "physical_memory" -> Set.of("manufacturer", "capacity", "configuredClockSpeed", "deviceLocator", "partNumber", "memoryType");
            case "disk_drive" -> Set.of("model", "manufacturer", "interfaceType", "size", "mediaType", "partitions", "firmwareRevisions");
            case "video_controller" -> Set.of("name", "adapterCompatibility", "adapterRAM", "videoProcessor", "driverVersion", "currentHorizontalResolution", "currentVerticalResolution");
            case "base_board" -> Set.of("manufacturer", "product", "version", "serialNumber");
            case "bios" -> Set.of("manufacturer", "sMBIOSBIOSVersion", "version", "releaseDate", "serialNumber");
            case "disk_partition" -> Set.of("name", "deviceID", "size", "startingOffset", "primaryPartition", "bootPartition");
            case "logical_disk" -> Set.of("deviceID", "volumeName", "fileSystem", "size", "freeSpace", "driveType");
            default -> Set.of();
        };
    }

    private Details createHardwareListSection(String type, List<?> values) {
        Details section = new Details();
        section.setSummaryText(message("computer_hardware_view.component_count",
                message("computer_hardware_view." + type), values.size()));
        if (values.isEmpty()) {
            section.add(new Span(message("computer_hardware_view.no_component_data")));
            return section;
        }
        VerticalLayout items = new VerticalLayout();
        items.setPadding(false);
        items.setSpacing(true);
        for (int index = 0; index < values.size(); index++) {
            List<HardwareEntity> properties = convert(values.get(index));
            String name = componentName(type, properties, index + 1);
            Details itemDetails = new Details();
            itemDetails.setSummaryText(name);
            addPropertyDetails(itemDetails, properties, importantPropertiesFor(type));
            items.add(itemDetails);
        }
        section.add(items);
        return section;
    }

    private Details createHardwareObjectSection(String type, Object value) {
        Details section = new Details();
        section.setSummaryText(message("computer_hardware_view." + type));
        addPropertyDetails(section, convert(value), importantPropertiesFor(type));
        return section;
    }

    private void addPropertyDetails(Details section, List<HardwareEntity> properties, Set<String> important) {
        if (properties.isEmpty()) {
            section.add(new Span(message("computer_hardware_view.no_component_data")));
            return;
        }
        List<HardwareEntity> keyProperties = properties.stream().filter(item -> important.contains(item.getName())).toList();
        List<HardwareEntity> otherProperties = properties.stream().filter(item -> !important.contains(item.getName())).toList();
        section.add(createTable(keyProperties.isEmpty() ? properties : keyProperties));
        if (!otherProperties.isEmpty()) {
            Details advanced = new Details();
            advanced.setSummaryText(message("computer_hardware_view.all_properties", otherProperties.size()));
            advanced.add(createTable(otherProperties));
            section.add(advanced);
        }
    }

    private List<HardwareEntity> convert(Object obj) {
        List<HardwareEntity> result = new ArrayList<>();
        if (obj == null) return result;
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String valueText = value instanceof String[] array ? String.join(", ", array)
                        : value == null ? "" : String.valueOf(value);
                if (valueText.isBlank() || "-".equals(valueText) || "Unknown".equalsIgnoreCase(valueText)) continue;
                result.add(new HardwareEntity(field.getName(), valueText));
            } catch (IllegalAccessException ignored) {
                // Ignore fields that cannot be read; the rest of the inventory remains useful.
            }
        }
        return result;
    }

    private String propertyLabel(String propertyName) {
        String key = "computer_hardware_view.property." + propertyName;
        return messageSource.getMessage(key, null, pascalToSpaced(propertyName), localeService.getCurrentLocale());
    }

    private String formatProperty(String propertyName, String value) {
        if (value == null || value.isBlank()) return message("computer_hardware_view.not_reported");
        return switch (propertyName) {
            case "totalPhysicalMemory", "capacity", "size", "freeSpace", "adapterRAM" -> {
                double bytes = parseDouble(value);
                yield bytes > 0 ? formatBytes(bytes) : value;
            }
            case "maxClockSpeed", "currentClockSpeed", "configuredClockSpeed" -> value + " MHz";
            case "l2CacheSize", "l3CacheSize" -> value + " KB";
            case "virtualizationFirmwareEnabled", "hypervisorPresent" -> Boolean.parseBoolean(value)
                    ? message("computer_hardware_view.yes") : message("computer_hardware_view.no");
            default -> value;
        };
    }

    private String formatBytes(double bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = 0;
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        NumberFormat format = NumberFormat.getNumberInstance(localeService.getCurrentLocale());
        format.setMaximumFractionDigits(1);
        format.setMinimumFractionDigits(value < 10 && unit > 0 ? 1 : 0);
        return format.format(value) + " " + units[unit];
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private String join(String first, String second) {
        if (first == null || first.isBlank()) return second == null ? "" : second;
        if (second == null || second.isBlank()) return first;
        return first + " " + second;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second == null ? "" : second;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String pascalToSpaced(String input) {
        if (input == null || input.isEmpty()) return input;
        String withSpaces = input.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
        return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1);
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args.length == 0 ? null : args, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return message("computer_hardware_view.title");
    }
}
