package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.SoftwareLicense;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@RolesAllowed("ADMIN")
@Route("inventory/licenses")
public class InventoryLicensesView extends VerticalLayout implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final IncidentService incidentService;
    private final MessageSource messages;
    private final LocaleService locale;
    private final Grid<SoftwareLicense> grid = new Grid<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private List<SoftwareLicense> licenses = List.of();

    public InventoryLicensesView(InventoryService inventoryService, IncidentService incidentService, MessageSource messages, LocaleService locale) {
        this.inventoryService = inventoryService; this.incidentService = incidentService; this.messages = messages; this.locale = locale;
        setSizeFull();
        Button add = new Button(msg("inventory_licenses_view.add"), e -> openEditor(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Anchor export = new Anchor(new StreamResource("software-licenses.csv", this::createCsv), msg("inventory_licenses_view.export_csv"));
        export.getElement().setAttribute("download", true);
        statusFilter.setItems("ALL", "COMPLIANT", "OVERUSED", "EXPIRED");
        statusFilter.setValue("ALL");
        statusFilter.setLabel(msg("inventory_licenses_view.status"));
        statusFilter.setItemLabelGenerator(value -> switch (value) {
            case "OVERUSED" -> msg("inventory_licenses_view.overused");
            case "EXPIRED" -> msg("inventory_licenses_view.expired");
            case "COMPLIANT" -> msg("inventory_licenses_view.compliant");
            default -> msg("inventory_licenses_view.all");
        });
        statusFilter.addValueChangeListener(event -> applyFilter());
        HorizontalLayout header = new HorizontalLayout(statusFilter, export, add);
        header.setWidthFull();
        header.setAlignItems(Alignment.END);
        grid.addColumn(SoftwareLicense::name).setHeader(msg("inventory_licenses_view.name")).setAutoWidth(true);
        grid.addColumn(SoftwareLicense::vendor).setHeader(msg("inventory_licenses_view.vendor")).setAutoWidth(true);
        grid.addColumn(SoftwareLicense::version).setHeader(msg("inventory_licenses_view.version")).setAutoWidth(true);
        grid.addColumn(SoftwareLicense::purchased).setHeader(msg("inventory_licenses_view.purchased")).setAutoWidth(true);
        grid.addColumn(SoftwareLicense::used).setHeader(msg("inventory_licenses_view.used")).setAutoWidth(true);
        grid.addColumn(item -> item.used() > item.purchased() ? msg("inventory_licenses_view.overused")
                        : item.expiresAt() != null && item.expiresAt().isBefore(java.time.LocalDate.now())
                        ? msg("inventory_licenses_view.expired") : msg("inventory_licenses_view.compliant"))
                .setHeader(msg("inventory_licenses_view.status")).setAutoWidth(true);
        grid.addColumn(item -> item.expiresAt() == null ? "-" : item.expiresAt().toString())
                .setHeader(msg("inventory_licenses_view.expires")).setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Button incident = new Button(msg("inventory_licenses_view.create_incident"));
            incident.setVisible(item.used() > item.purchased() || item.expiresAt() != null && item.expiresAt().isBefore(java.time.LocalDate.now()));
            incident.addClickListener(e -> confirmIncident(item));
            Button delete = new Button(msg("common.delete"), e -> { if (inventoryService.deleteLicense(item.id())) refresh(); });
            return new HorizontalLayout(incident, delete);
        }).setHeader(msg("common.actions")).setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setSizeFull();
        add(header, grid); expand(grid); refresh();
    }

    private void refresh() { licenses = inventoryService.getLicenses(); applyFilter(); }

    private void applyFilter() {
        String selected = statusFilter.getValue() == null ? "ALL" : statusFilter.getValue();
        grid.setItems(licenses.stream().filter(item -> "ALL".equals(selected) || selected.equals(status(item))).toList());
    }

    private String status(SoftwareLicense item) {
        return item.used() > item.purchased() ? "OVERUSED"
                : item.expiresAt() != null && item.expiresAt().isBefore(java.time.LocalDate.now()) ? "EXPIRED" : "COMPLIANT";
    }

    private ByteArrayInputStream createCsv() {
        StringBuilder csv = new StringBuilder("Software,Vendor,Version,Purchased,Used,Expires,Status\n");
        inventoryService.getLicenses().forEach(item -> {
            String status = msg("inventory_licenses_view." + status(item).toLowerCase());
            csv.append(csv(item.name())).append(',').append(csv(item.vendor())).append(',').append(csv(item.version())).append(',')
                    .append(item.purchased()).append(',').append(item.used()).append(',').append(csv(String.valueOf(item.expiresAt())))
                    .append(',').append(csv(status)).append('\n');
        });
        return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csv(String value) { return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\""; }

    private void openEditor(SoftwareLicense current) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(msg("inventory_licenses_view.add"));
        dialog.setWidth("520px");
        dialog.setMaxWidth("calc(100vw - 32px)");
        ComboBox<SoftwareCount> discoveredSoftware = new ComboBox<>(msg("inventory_licenses_view.select_discovered_software"));
        discoveredSoftware.setPlaceholder(msg("inventory_licenses_view.discovered_software_placeholder"));
        discoveredSoftware.setWidthFull();
        discoveredSoftware.setClearButtonVisible(true);
        discoveredSoftware.setPageSize(30);
        discoveredSoftware.setItemLabelGenerator(item -> {
            String label = String.join(" — ", java.util.stream.Stream.of(item.getName(), item.getVendor(), item.getVersion())
                    .filter(value -> value != null && !value.isBlank()).toList());
            return label + " (" + msg("inventory_licenses_view.detected_installations", new Object[]{item.getCount()}) + ")";
        });
        discoveredSoftware.setItems(query -> {
            String term = query.getFilter().orElse("");
            return inventoryService.getDiscoveredSoftware(PageRequest.of(query.getPage(), query.getPageSize()), term).stream();
        });
        TextField name = new TextField(msg("inventory_licenses_view.name"));
        TextField vendor = new TextField(msg("inventory_licenses_view.vendor"));
        TextField version = new TextField(msg("inventory_licenses_view.version"));
        name.setWidthFull(); vendor.setWidthFull(); version.setWidthFull();
        Span detectedUsage = new Span(msg("inventory_licenses_view.discovered_software_hint"));
        discoveredSoftware.addValueChangeListener(event -> {
            SoftwareCount selected = event.getValue();
            if (selected == null) {
                detectedUsage.setText(msg("inventory_licenses_view.discovered_software_hint"));
                return;
            }
            name.setValue(selected.getName() == null ? "" : selected.getName());
            vendor.setValue(selected.getVendor() == null ? "" : selected.getVendor());
            version.setValue(selected.getVersion() == null ? "" : selected.getVersion());
            detectedUsage.setText(msg("inventory_licenses_view.detected_installations", new Object[]{selected.getCount()}));
        });
        if (current != null) {
            name.setValue(current.name() == null ? "" : current.name());
            vendor.setValue(current.vendor() == null ? "" : current.vendor());
            version.setValue(current.version() == null ? "" : current.version());
        }
        IntegerField purchased = new IntegerField(msg("inventory_licenses_view.purchased"));
        purchased.setMin(0); purchased.setValue(0);
        DatePicker expires = new DatePicker(msg("inventory_licenses_view.expires"));
        TextArea notes = new TextArea(msg("inventory_licenses_view.notes"));
        purchased.setWidthFull(); expires.setWidthFull(); notes.setWidthFull();
        Button save = new Button(msg("common.save"), e -> {
            if (name.isEmpty()) { name.setInvalid(true); return; }
            SoftwareLicense saved = inventoryService.saveLicense(new SoftwareLicense(current == null ? null : current.id(),
                    name.getValue(), vendor.getValue(), version.getValue(), purchased.getValue() == null ? 0 : purchased.getValue(), 0,
                    expires.getValue(), notes.getValue()));
            if (saved != null) { dialog.close(); refresh(); Notification.show(msg("inventory_licenses_view.saved")); }
        });
        Button cancel = new Button(msg("common.cancel"), e -> dialog.close());
        VerticalLayout form = new VerticalLayout(discoveredSoftware, detectedUsage, name, vendor, version,
                purchased, expires, notes, new HorizontalLayout(save, cancel));
        form.setWidthFull();
        dialog.add(form);
        dialog.open();
    }

    private void confirmIncident(SoftwareLicense license) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(msg("inventory_licenses_view.create_incident"));
        dialog.setText(msg("inventory_licenses_view.create_incident_confirm"));
        dialog.setCancelable(true);
        dialog.setConfirmText(msg("inventory_licenses_view.create_incident"));
        dialog.addConfirmListener(event -> {
            IncidentItem incident = new IncidentItem();
            incident.setSignalId("SOFTWARE_LICENSE_COMPLIANCE");
            incident.setName("Software license issue: " + license.name());
            incident.setSeverity(Severity.HIGH);
            incident.setStatus(IncidentStatus.OPEN);
            incident.setEventCount(1);
            incident.setRecommendation("Review software licensing and remove unauthorized installations or renew the license.");
            incident.setContext("Used: " + license.used() + ", purchased: " + license.purchased()
                    + ", expires: " + license.expiresAt());
            incident.setMeta(false);
            incident.setCreatedAt(LocalDateTime.now());
            incident.setDeduplicationKey("license-compliance-" + license.id() + "-" + UUID.randomUUID());
            incidentService.createIncident(incident);
            Notification.show(msg("inventory_licenses_view.incident_created"));
        });
        dialog.open();
    }

    private String msg(String key) { return messages.getMessage(key, null, locale.getCurrentLocale()); }
    private String msg(String key, Object[] arguments) { return messages.getMessage(key, arguments, locale.getCurrentLocale()); }
    @Override public String getPageTitle() { return msg("inventory_licenses_view.title"); }
}
