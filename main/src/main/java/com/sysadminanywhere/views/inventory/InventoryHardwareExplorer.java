package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.HardwareModelItem;
import com.sysadminanywhere.common.inventory.model.HardwarePropertyItem;
import com.sysadminanywhere.control.HardwareExplorer;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import org.springframework.context.MessageSource;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Builds the same category browser for saved inventory snapshots as the live Management page. */
final class InventoryHardwareExplorer {
    private final MessageSource messageSource;
    private final LocaleService localeService;

    InventoryHardwareExplorer(MessageSource messageSource, LocaleService localeService) {
        this.messageSource = messageSource;
        this.localeService = localeService;
    }

    HardwareExplorer createExplorer(List<HardwareModelItem> components) {
        return new HardwareExplorer(msg("computer_hardware_view.components"), List.of(
                section("overview", () -> overview(components)),
                section("operating_system", () -> category(components, "operatingsystem")),
                section("processor", () -> category(components, "processor")),
                section("physical_memory", () -> category(components, "physicalmemory")),
                section("base_board", () -> category(components, "baseboard", "bios", "computersystem")),
                section("video_controller", () -> category(components, "videocontroller")),
                section("storage", () -> category(components, "diskdrive", "diskpartition", "logicaldisk")),
                section("optical_drive", () -> category(components, "opticaldrive")),
                section("audio", () -> category(components, "sounddevice")),
                section("peripherals", () -> category(components, "keyboard", "pointingdevice")),
                section("network", () -> category(components, "networkadapter"))));
    }

    private HardwareExplorer.Section section(String key, java.util.function.Supplier<com.vaadin.flow.component.Component> content) {
        return new HardwareExplorer.Section(key, msg("computer_hardware_view." + key), content);
    }

    private com.vaadin.flow.component.Component overview(List<HardwareModelItem> components) {
        Div rows = new Div();
        rows.addClassName("hardware-overview-list");
        for (HardwareModelItem component : components) {
            if ("patch".equals(normalizeType(component.getType()))) continue;
            Div row = new Div(new Span(typeLabel(component.getType())), new Span(display(component.getName())));
            row.addClassName("hardware-overview-row");
            rows.add(row);
        }
        return rows;
    }

    private com.vaadin.flow.component.Component category(List<HardwareModelItem> components, String... types) {
        Div list = new Div();
        for (HardwareModelItem component : components) {
            String normalized = normalizeType(component.getType());
            if (java.util.Arrays.stream(types).noneMatch(normalized::equals)) continue;
            Div card = new Div();
            card.addClassName("hardware-component");
            card.add(new H3(display(component.getName())));
            if (component.getProperties() != null) {
                List<HardwarePropertyItem> properties = component.getProperties().stream()
                        .filter(property -> property.getPropertyName() != null && !property.getPropertyName().startsWith("__"))
                        .filter(property -> property.getPropertyValue() != null && !property.getPropertyValue().isBlank())
                        .toList();
                Set<String> preferred = preferredProperties(normalized);
                List<HardwarePropertyItem> main = properties.stream()
                        .filter(property -> preferred.contains(property.getPropertyName().toLowerCase(Locale.ROOT))).toList();
                if (main.isEmpty()) main = properties.stream().limit(8).toList();
                addProperties(card, main);
                List<HardwarePropertyItem> selected = main;
                List<HardwarePropertyItem> rest = properties.stream().filter(property -> !selected.contains(property)).toList();
                if (!rest.isEmpty()) {
                    Div advancedContent = new Div();
                    addProperties(advancedContent, rest);
                    card.add(new Details(message("computer_hardware_view.all_properties", rest.size()), advancedContent));
                }
            }
            list.add(card);
        }
        if (list.getChildren().findAny().isEmpty()) {
            Span empty = new Span(msg("computer_hardware_view.not_collected"));
            empty.addClassName("hardware-explorer-empty");
            return empty;
        }
        return list;
    }

    private void addProperties(Div container, List<HardwarePropertyItem> properties) {
        for (HardwarePropertyItem property : properties) {
            Div row = new Div(new Span(propertyLabel(property.getPropertyName())),
                    new Span(formatProperty(property.getPropertyName(), property.getPropertyValue())));
            row.addClassName("hardware-overview-row");
            container.add(row);
        }
    }

    private Set<String> preferredProperties(String type) {
        return switch (type) {
            case "operatingsystem" -> Set.of("caption", "version", "osarchitecture", "csdversion", "manufacturer");
            case "processor" -> Set.of("name", "manufacturer", "numberofcores", "numberoflogicalprocessors",
                    "maxclockspeed", "currentclockspeed", "socketdesignation");
            case "physicalmemory" -> Set.of("capacity", "configuredclockspeed", "devicelocator", "partnumber", "manufacturer");
            case "baseboard" -> Set.of("manufacturer", "product", "version", "serialnumber");
            case "bios" -> Set.of("manufacturer", "smbiosbiosversion", "version", "releasedate");
            case "computersystem" -> Set.of("manufacturer", "model", "totalphysicalmemory", "systemtype");
            case "videocontroller" -> Set.of("name", "adapterram", "videoprocessor", "driverversion",
                    "currenthorizontalresolution", "currentverticalresolution", "currentrefreshrate");
            case "diskdrive" -> Set.of("model", "size", "interfacetype", "mediatype", "manufacturer");
            case "logicaldisk" -> Set.of("deviceid", "volumename", "filesystem", "size", "freespace");
            case "opticaldrive" -> Set.of("name", "drive", "mediatype", "manufacturer", "status");
            case "sounddevice" -> Set.of("name", "manufacturer", "status", "productname");
            case "keyboard", "pointingdevice" -> Set.of("name", "manufacturer", "description", "status");
            case "networkadapter" -> Set.of("name", "manufacturer", "macaddress", "adaptertype", "speed", "netenabled");
            default -> Set.of();
        };
    }

    private String formatProperty(String name, String value) {
        String normalized = name.toLowerCase(Locale.ROOT);
        if (Set.of("capacity", "size", "freespace", "totalphysicalmemory", "adapterram").contains(normalized)) {
            try {
                double amount = Double.parseDouble(value.trim());
                String[] units = {"B", "KB", "MB", "GB", "TB"};
                int index = 0;
                while (amount >= 1024 && index < units.length - 1) { amount /= 1024; index++; }
                NumberFormat format = NumberFormat.getNumberInstance(localeService.getCurrentLocale());
                format.setMaximumFractionDigits(1);
                return format.format(amount) + " " + units[index];
            } catch (NumberFormatException ignored) { return value; }
        }
        if (Set.of("maxclockspeed", "currentclockspeed", "configuredclockspeed", "currentrefreshrate").contains(normalized))
            return value + ("currentrefreshrate".equals(normalized) ? " Hz" : " MHz");
        if (Set.of("l2cachesize", "l3cachesize").contains(normalized)) return value + " KB";
        if ("true".equalsIgnoreCase(value)) return msg("computer_hardware_view.yes");
        if ("false".equalsIgnoreCase(value)) return msg("computer_hardware_view.no");
        return value;
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    String typeLabel(String type) {
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

    String propertyLabel(String value) {
        if (value == null) return "";
        String fallback = value.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
        String key = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        return messageSource.getMessage("computer_hardware_view.property." + key, null, fallback,
                localeService.getCurrentLocale());
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, localeService.getCurrentLocale());
    }
}
