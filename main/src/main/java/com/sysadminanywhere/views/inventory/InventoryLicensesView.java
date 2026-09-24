package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.SoftwareLicense;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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

@RolesAllowed("ADMIN")
@Route("inventory/licenses")
public class InventoryLicensesView extends VerticalLayout implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final MessageSource messages;
    private final LocaleService locale;
    private final Grid<SoftwareLicense> grid = new Grid<>();

    public InventoryLicensesView(InventoryService inventoryService, MessageSource messages, LocaleService locale) {
        this.inventoryService = inventoryService; this.messages = messages; this.locale = locale;
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
        grid.addColumn(item -> item.expiresAt() == null ? "-" : item.expiresAt().toString())
                .setHeader(msg("inventory_licenses_view.expires")).setAutoWidth(true);
        grid.addComponentColumn(item -> new Button(msg("common.delete"), e -> {
            if (inventoryService.deleteLicense(item.id())) refresh();
        })).setHeader(msg("common.actions")).setAutoWidth(true);
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
                    name.getValue(), vendor.getValue(), version.getValue(), purchased.getValue() == null ? 0 : purchased.getValue(),
                    expires.getValue(), notes.getValue()));
            if (saved != null) { dialog.close(); refresh(); Notification.show(msg("inventory_licenses_view.saved")); }
        });
        Button cancel = new Button(msg("common.cancel"), e -> dialog.close());
        dialog.add(new VerticalLayout(name, vendor, version, purchased, expires, notes, new HorizontalLayout(save, cancel)));
        dialog.open();
    }

    private String msg(String key) { return messages.getMessage(key, null, locale.getCurrentLocale()); }
    @Override public String getPageTitle() { return msg("inventory_licenses_view.title"); }
}
