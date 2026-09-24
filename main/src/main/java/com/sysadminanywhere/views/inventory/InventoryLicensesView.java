package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.SoftwareLicense;
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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.util.List;
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

    public InventoryLicensesView(InventoryService inventoryService, IncidentService incidentService, MessageSource messages, LocaleService locale) {
        this.inventoryService = inventoryService; this.incidentService = incidentService; this.messages = messages; this.locale = locale;
        setSizeFull();
        H2 title = new H2(msg("inventory_licenses_view.title"));
        Button add = new Button(msg("inventory_licenses_view.add"), e -> openEditor(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout header = new HorizontalLayout(title, add);
        header.setWidthFull(); header.setFlexGrow(1, title);
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
        grid.setSizeFull();
        add(header, grid); expand(grid); refresh();
    }

    private void refresh() { grid.setItems(inventoryService.getLicenses()); }

    private void openEditor(SoftwareLicense current) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(msg("inventory_licenses_view.add"));
        TextField name = new TextField(msg("inventory_licenses_view.name"));
        TextField vendor = new TextField(msg("inventory_licenses_view.vendor"));
        TextField version = new TextField(msg("inventory_licenses_view.version"));
        IntegerField purchased = new IntegerField(msg("inventory_licenses_view.purchased"));
        purchased.setMin(0); purchased.setValue(0);
        DatePicker expires = new DatePicker(msg("inventory_licenses_view.expires"));
        TextArea notes = new TextArea(msg("inventory_licenses_view.notes"));
        Button save = new Button(msg("common.save"), e -> {
            if (name.isEmpty()) { name.setInvalid(true); return; }
            SoftwareLicense saved = inventoryService.saveLicense(new SoftwareLicense(current == null ? null : current.id(),
                    name.getValue(), vendor.getValue(), version.getValue(), purchased.getValue() == null ? 0 : purchased.getValue(), 0,
                    expires.getValue(), notes.getValue()));
            if (saved != null) { dialog.close(); refresh(); Notification.show(msg("inventory_licenses_view.saved")); }
        });
        Button cancel = new Button(msg("common.cancel"), e -> dialog.close());
        dialog.add(new VerticalLayout(name, vendor, version, purchased, expires, notes, new HorizontalLayout(save, cancel)));
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
    @Override public String getPageTitle() { return msg("inventory_licenses_view.title"); }
}
