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
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.progressbar.ProgressBar;
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

import java.util.List;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@RolesAllowed("ADMIN")
@Route("inventory/licenses")
public class InventoryLicensesView extends VerticalLayout implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final IncidentService incidentService;
    private final MessageSource messages;
    private final LocaleService locale;
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final TextField searchFilter = new TextField();
    private final Div licenseList = new Div();
    private final Span totalCount = new Span();
    private final Span compliantCount = new Span();
    private final Span attentionCount = new Span();
    private List<SoftwareLicense> licenses = List.of();

    public InventoryLicensesView(InventoryService inventoryService, IncidentService incidentService, MessageSource messages, LocaleService locale) {
        this.inventoryService = inventoryService; this.incidentService = incidentService; this.messages = messages; this.locale = locale;
        setWidthFull();
        addClassName("review-page");
        setPadding(false);
        setSpacing(true);
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
        searchFilter.setPlaceholder(msg("inventory_licenses_view.search_placeholder"));
        searchFilter.setAriaLabel(msg("inventory_licenses_view.search_placeholder"));
        searchFilter.setClearButtonVisible(true);
        searchFilter.addValueChangeListener(event -> applyFilter());
        Span intro = new Span(msg("inventory_licenses_view.subtitle"));
        intro.addClassName("review-intro-text");
        HorizontalLayout header = new HorizontalLayout(export, add);
        header.addClassName("review-toolbar");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        HorizontalLayout summary = new HorizontalLayout(
                metric(msg("inventory_licenses_view.total_count"), totalCount),
                metric(msg("inventory_licenses_view.compliant"), compliantCount),
                metric(msg("inventory_licenses_view.attention_count"), attentionCount));
        summary.addClassName("license-summary");
        summary.setWidthFull();
        Div records = new Div();
        records.addClassName("license-records");
        HorizontalLayout filters = new HorizontalLayout(searchFilter, statusFilter);
        filters.addClassName("review-filter-bar");
        filters.setWidthFull();
        licenseList.addClassName("license-list");
        records.add(new H3(msg("inventory_licenses_view.records")), filters, licenseList);
        add(intro, header, summary, records);
        refresh();
    }

    private Card metric(String label, Span value) {
        Card card = new Card();
        card.addClassName("review-stat-card");
        Span caption = new Span(label);
        caption.addClassName("review-stat-label");
        value.addClassName("review-stat-value");
        VerticalLayout stack = new VerticalLayout(caption, value);
        stack.setPadding(false);
        stack.setSpacing(false);
        card.add(stack);
        return card;
    }

    private Span licenseBadge(String value) {
        Span badge = new Span(msg("inventory_licenses_view." + value.toLowerCase(Locale.ROOT)));
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add("COMPLIANT".equals(value) ? "success" : "error");
        return badge;
    }

    private Card licenseCard(SoftwareLicense item) {
        Card card = new Card();
        card.addClassName("license-item");
        if (needsAttention(item)) card.addClassName("license-item-attention");

        Div heading = new Div();
        heading.addClassName("license-item-heading");
        Div identity = new Div();
        H3 name = new H3(item.name() == null ? "—" : item.name());
        Span meta = new Span(String.join(" · ", java.util.stream.Stream.of(item.vendor(), item.version())
                .filter(value -> value != null && !value.isBlank()).toList()));
        meta.addClassName("license-item-meta");
        identity.add(name, meta);
        Div badges = new Div();
        badges.addClassName("license-item-badges");
        if (overused(item)) badges.add(licenseBadge("OVERUSED"));
        if (expired(item)) badges.add(licenseBadge("EXPIRED"));
        if (!needsAttention(item)) badges.add(licenseBadge("COMPLIANT"));
        heading.add(identity, badges);

        Div usage = new Div();
        usage.addClassName("license-item-usage");
        Span usageLabel = new Span(msg("inventory_licenses_view.usage"));
        Span usageValue = new Span(msg("inventory_licenses_view.usage_count", item.used(), item.purchased()));
        usageValue.addClassName("license-item-usage-value");
        ProgressBar progress = new ProgressBar();
        progress.setValue(item.purchased() <= 0 ? (item.used() > 0 ? 1 : 0)
                : Math.min(1.0, item.used() / (double) item.purchased()));
        progress.getElement().setAttribute("aria-label", msg("inventory_licenses_view.usage"));
        if (overused(item)) progress.addClassName("license-usage-overused");
        usage.add(usageLabel, usageValue, progress);

        Div footer = new Div();
        footer.addClassName("license-item-footer");
        Span expiry = new Span(msg("inventory_licenses_view.expires") + ": "
                + (item.expiresAt() == null ? msg("inventory_licenses_view.no_expiry") : item.expiresAt()));
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("license-item-actions");
        Button edit = new Button(msg("inventory_licenses_view.edit"), event -> openEditor(item));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.getElement().setAttribute("aria-label", msg("inventory_licenses_view.edit") + " " + item.name());
        actions.add(edit);
        if (needsAttention(item)) {
            Button incident = new Button(msg("inventory_licenses_view.create_incident"), event -> confirmIncident(item));
            incident.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            actions.add(incident);
        }
        Button delete = new Button(msg("common.delete"), event -> confirmDelete(item));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        delete.getElement().setAttribute("aria-label", msg("common.delete") + " " + item.name());
        actions.add(delete);
        footer.add(expiry, actions);
        Div body = new Div(heading, usage);
        if (item.notes() != null && !item.notes().isBlank()) {
            Span notes = new Span(item.notes());
            notes.addClassName("license-item-notes");
            body.add(notes);
        }
        body.add(footer);
        body.addClassName("license-item-body");
        card.add(body);
        return card;
    }

    private void confirmDelete(SoftwareLicense item) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(msg("inventory_licenses_view.delete_title"));
        dialog.setText(msg("inventory_licenses_view.delete_confirm", item.name()));
        dialog.setCancelable(true);
        dialog.setConfirmText(msg("common.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            if (inventoryService.deleteLicense(item.id())) refresh();
        });
        dialog.open();
    }

    private void refresh() {
        licenses = inventoryService.getLicenses();
        long compliant = licenses.stream().filter(item -> !needsAttention(item)).count();
        totalCount.setText(String.valueOf(licenses.size()));
        compliantCount.setText(String.valueOf(compliant));
        attentionCount.setText(String.valueOf(licenses.size() - compliant));
        applyFilter();
    }

    private void applyFilter() {
        String selected = statusFilter.getValue() == null ? "ALL" : statusFilter.getValue();
        String query = searchFilter.getValue() == null ? "" : searchFilter.getValue().trim().toLowerCase(Locale.ROOT);
        List<SoftwareLicense> visible = licenses.stream()
                .filter(item -> "ALL".equals(selected) || "COMPLIANT".equals(selected) && !needsAttention(item)
                        || "OVERUSED".equals(selected) && overused(item)
                        || "EXPIRED".equals(selected) && expired(item))
                .filter(item -> query.isBlank() || java.util.stream.Stream.of(item.name(), item.vendor(), item.version())
                        .filter(value -> value != null && value.toLowerCase(Locale.ROOT).contains(query)).findAny().isPresent())
                .toList();
        licenseList.removeAll();
        if (visible.isEmpty()) {
            Span empty = new Span(msg(licenses.isEmpty() ? "inventory_licenses_view.empty" : "inventory_licenses_view.no_matches"));
            empty.addClassName("license-empty");
            licenseList.add(empty);
        } else {
            visible.forEach(item -> licenseList.add(licenseCard(item)));
        }
    }

    private String status(SoftwareLicense item) {
        return overused(item) ? "OVERUSED" : expired(item) ? "EXPIRED" : "COMPLIANT";
    }

    private boolean overused(SoftwareLicense item) { return item.used() > item.purchased(); }
    private boolean expired(SoftwareLicense item) { return item.expiresAt() != null && item.expiresAt().isBefore(LocalDate.now()); }
    private boolean needsAttention(SoftwareLicense item) { return overused(item) || expired(item); }

    private ByteArrayInputStream createCsv() {
        StringBuilder csv = new StringBuilder("Software,Vendor,Version,Purchased,Used,Expires,Status\n");
        inventoryService.getLicenses().forEach(item -> {
            String status = overused(item) && expired(item)
                    ? msg("inventory_licenses_view.overused") + ", " + msg("inventory_licenses_view.expired")
                    : msg("inventory_licenses_view." + status(item).toLowerCase(Locale.ROOT));
            csv.append(csv(item.name())).append(',').append(csv(item.vendor())).append(',').append(csv(item.version())).append(',')
                    .append(item.purchased()).append(',').append(item.used()).append(',').append(csv(String.valueOf(item.expiresAt())))
                    .append(',').append(csv(status)).append('\n');
        });
        return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csv(String value) { return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\""; }

    private void openEditor(SoftwareLicense current) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(msg(current == null ? "inventory_licenses_view.add" : "inventory_licenses_view.edit"));
        dialog.setWidth("min(520px, calc(100vw - 32px))");
        dialog.setMaxWidth("calc(100vw - 32px)");
        ComboBox<SoftwareCount> discoveredSoftware = new ComboBox<>(msg("inventory_licenses_view.select_discovered_software"));
        discoveredSoftware.setPlaceholder(msg("inventory_licenses_view.discovered_software_placeholder"));
        discoveredSoftware.setWidthFull();
        discoveredSoftware.setVisible(current == null);
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
        detectedUsage.setVisible(current == null);
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
        purchased.setMin(0); purchased.setValue(current == null ? 0 : Math.toIntExact(current.purchased()));
        DatePicker expires = new DatePicker(msg("inventory_licenses_view.expires"));
        TextArea notes = new TextArea(msg("inventory_licenses_view.notes"));
        if (current != null) {
            if (current.expiresAt() != null) expires.setValue(current.expiresAt());
            notes.setValue(current.notes() == null ? "" : current.notes());
        }
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
    private String msg(String key, Object... arguments) { return messages.getMessage(key, arguments, locale.getCurrentLocale()); }
    @Override public String getPageTitle() { return msg("inventory_licenses_view.title"); }
}
