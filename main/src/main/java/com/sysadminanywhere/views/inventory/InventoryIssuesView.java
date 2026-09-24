package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.InventoryHealthComputer;
import com.sysadminanywhere.common.inventory.model.SoftwareLicense;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RolesAllowed({"ADMIN", "READER"})
@Route("inventory/issues")
public class InventoryIssuesView extends VerticalLayout implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final IncidentService incidentService;
    private final MessageSource messages;
    private final LocaleService locale;
    private final Grid<Issue> grid = new Grid<>();
    private final ComboBox<String> severityFilter = new ComboBox<>();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private List<Issue> issues = List.of();
    private final Span highCount = new Span();
    private final Span mediumCount = new Span();

    public InventoryIssuesView(InventoryService inventoryService, IncidentService incidentService, MessageSource messages, LocaleService locale) {
        this.inventoryService = inventoryService; this.incidentService = incidentService; this.messages = messages; this.locale = locale;
        setSizeFull();
        H2 title = new H2(msg("inventory_issues_view.title"));
        Button refresh = new Button(msg("common.refresh"), e -> refresh());
        severityFilter.setItems("ALL", "HIGH", "MEDIUM");
        severityFilter.setValue("ALL");
        severityFilter.setLabel(msg("inventory_issues_view.severity"));
        severityFilter.addValueChangeListener(event -> applyFilter());
        typeFilter.setItems("ALL", "INVENTORY", "LICENSE");
        typeFilter.setValue("ALL");
        typeFilter.setLabel(msg("inventory_issues_view.type"));
        typeFilter.setItemLabelGenerator(value -> switch (value) {
            case "INVENTORY" -> msg("inventory_issues_view.inventory");
            case "LICENSE" -> msg("inventory_issues_view.license");
            default -> msg("inventory_issues_view.all");
        });
        typeFilter.addValueChangeListener(event -> applyFilter());
        highCount.getStyle().set("color", "var(--lumo-error-text-color)").set("font-weight", "600");
        mediumCount.getStyle().set("color", "var(--lumo-warning-text-color)").set("font-weight", "600");
        HorizontalLayout summary = new HorizontalLayout(highCount, mediumCount);
        Button createIncidents = new Button(msg("inventory_issues_view.create_incidents"), e -> confirmCreateIncidents());
        HorizontalLayout header = new HorizontalLayout(title, summary, typeFilter, severityFilter, createIncidents, refresh);
        header.setFlexGrow(1, title);
        header.setWidthFull(); header.setFlexGrow(1, title);
        grid.addColumn(Issue::severity).setHeader(msg("inventory_issues_view.severity")).setAutoWidth(true);
        grid.addColumn(Issue::type).setHeader(msg("inventory_issues_view.type")).setAutoWidth(true);
        grid.addColumn(Issue::object).setHeader(msg("inventory_issues_view.object")).setAutoWidth(true);
        grid.addColumn(Issue::details).setHeader(msg("inventory_issues_view.details")).setFlexGrow(1);
        grid.addComponentColumn(item -> new Anchor(item.actionKey().equals("open_licenses") ? "inventory/licenses" : "inventory/health",
                        msg("inventory_issues_view." + item.actionKey())))
                .setHeader(msg("inventory_issues_view.action")).setAutoWidth(true);
        grid.setSizeFull();
        add(header, grid); expand(grid); refresh();
    }

    private void refresh() {
        List<Issue> issues = new ArrayList<>();
        var health = inventoryService.getInventoryHealth(30);
        if (health != null && health.computers() != null) {
            for (InventoryHealthComputer computer : health.computers()) {
                if ("ERROR".equalsIgnoreCase(computer.scanStatus())) {
                    issues.add(new Issue("HIGH", msg("inventory_issues_view.inventory"), computer.name(),
                            computer.scanError(), "open_inventory"));
                } else {
                    issues.add(new Issue("MEDIUM", msg("inventory_issues_view.inventory"), computer.name(),
                            msg("inventory_issues_view.stale_details"), "open_inventory"));
                }
            }
        }
        for (SoftwareLicense license : inventoryService.getLicenses()) {
            if (license.used() > license.purchased()) {
                issues.add(new Issue("HIGH", msg("inventory_issues_view.license"), license.name(),
                        msg("inventory_issues_view.overuse_details"), "open_licenses"));
            } else if (license.expiresAt() != null && license.expiresAt().isBefore(LocalDate.now())) {
                issues.add(new Issue("HIGH", msg("inventory_issues_view.license"), license.name(),
                        msg("inventory_issues_view.expired_details"), "open_licenses"));
            }
        }
        var coverage = inventoryService.getInventoryCoverage();
        if (coverage.softwareWithoutVersion() > 0) {
            issues.add(new Issue("MEDIUM", msg("inventory_issues_view.inventory"),
                    msg("inventory_issues_view.software_versions"),
                    msg("inventory_issues_view.software_versions_details", coverage.softwareWithoutVersion()),
                    "open_inventory"));
        }
        this.issues = issues;
        highCount.setText(msg("inventory_issues_view.high_count") + ": "
                + issues.stream().filter(item -> "HIGH".equals(item.severity())).count());
        mediumCount.setText(msg("inventory_issues_view.medium_count") + ": "
                + issues.stream().filter(item -> "MEDIUM".equals(item.severity())).count());
        applyFilter();
    }

    private void applyFilter() {
        String selected = severityFilter.getValue() == null ? "ALL" : severityFilter.getValue();
        String selectedType = typeFilter.getValue() == null ? "ALL" : typeFilter.getValue();
        grid.setItems(issues.stream()
                .filter(item -> "ALL".equals(selected) || selected.equals(item.severity()))
                .filter(item -> "ALL".equals(selectedType) || selectedType.equals(item.actionKey().equals("open_licenses") ? "LICENSE" : "INVENTORY"))
                .toList());
    }

    private void confirmCreateIncidents() {
        String selected = severityFilter.getValue() == null ? "ALL" : severityFilter.getValue();
        String selectedType = typeFilter.getValue() == null ? "ALL" : typeFilter.getValue();
        List<Issue> visible = issues.stream()
                .filter(item -> "ALL".equals(selected) || selected.equals(item.severity()))
                .filter(item -> "ALL".equals(selectedType) || selectedType.equals(item.actionKey().equals("open_licenses") ? "LICENSE" : "INVENTORY"))
                .toList();
        if (visible.isEmpty()) return;
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(msg("inventory_issues_view.create_incidents"));
        dialog.setText(msg("inventory_issues_view.create_incidents_confirm", visible.size()));
        dialog.setCancelable(true);
        dialog.setConfirmText(msg("inventory_issues_view.create_incidents"));
        dialog.addConfirmListener(event -> {
            visible.forEach(item -> {
                IncidentItem incident = new IncidentItem();
                incident.setSignalId("INVENTORY_ISSUE");
                incident.setName(item.type() + ": " + item.object());
                incident.setSeverity("HIGH".equals(item.severity()) ? Severity.HIGH : Severity.MEDIUM);
                incident.setStatus(IncidentStatus.OPEN); incident.setEventCount(1);
                incident.setRecommendation(item.details()); incident.setContext(item.details());
                incident.setMeta(false); incident.setCreatedAt(LocalDateTime.now());
                if ("open_inventory".equals(item.actionKey())) incident.setMachineName(item.object());
                incident.setDeduplicationKey("inventory-issue-" + item.actionKey() + "-"
                        + item.object().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                        + "-" + Integer.toHexString(item.object().hashCode())
                        + "-" + item.severity().toLowerCase(Locale.ROOT));
                incidentService.createIncident(incident);
            });
        });
        dialog.open();
    }

    private String msg(String key) { return messages.getMessage(key, null, locale.getCurrentLocale()); }
    private String msg(String key, Object... args) { return messages.getMessage(key, args, locale.getCurrentLocale()); }
    @Override public String getPageTitle() { return msg("inventory_issues_view.title"); }
    private record Issue(String severity, String type, String object, String details, String actionKey) { }
}
