package com.sysadminanywhere.views.inventory;

import com.sysadminanywhere.common.inventory.model.InventoryHealthComputer;
import com.sysadminanywhere.common.inventory.model.InventoryHealthDto;
import com.sysadminanywhere.common.inventory.model.InventoryScanRun;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "inventory/health")
public class InventoryHealthView extends VerticalLayout implements HasDynamicTitle {
    private final InventoryService inventoryService;
    private final IncidentService incidentService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final IntegerField staleDays = new IntegerField();
    private final Span total = new Span();
    private final Span stale = new Span();
    private final Span neverScanned = new Span();
    private final Span scanStatus = new Span();
    private final Grid<InventoryHealthComputer> grid = new Grid<>();
    private final Grid<InventoryScanRun> historyGrid = new Grid<>();
    private final TextField computerFilter = new TextField();
    private List<InventoryHealthComputer> currentComputers = List.of();
    private final Button scanSelected = new Button();
    private final Button retryFailed = new Button();
    private final Button cancelScan = new Button();
    private final Anchor export = new Anchor();

    public InventoryHealthView(InventoryService inventoryService, IncidentService incidentService,
                               MessageSource messageSource, LocaleService localeService) {
        this.inventoryService = inventoryService;
        this.incidentService = incidentService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(message("inventory_health_view.title"));
        staleDays.setLabel(message("inventory_health_view.stale_days"));
        staleDays.setMin(1); staleDays.setMax(3650); staleDays.setValue(30);
        staleDays.setWidth("150px");
        computerFilter.setPlaceholder(message("inventory_health_view.filter_placeholder"));
        computerFilter.setClearButtonVisible(true);
        computerFilter.setWidth("220px");
        computerFilter.addValueChangeListener(event -> applyFilter());
        Button refresh = new Button(message("common.refresh"), event -> refresh());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button startScan = new Button(message("inventory_health_view.start_scan"), event -> startScan());
        startScan.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        startScan.setVisible(SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        scanSelected.setText(message("inventory_health_view.scan_selected"));
        scanSelected.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        scanSelected.setVisible(startScan.isVisible());
        scanSelected.addClickListener(event -> startSelectedScan());
        retryFailed.setText(message("inventory_health_view.retry_failed"));
        retryFailed.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        retryFailed.setVisible(startScan.isVisible());
        retryFailed.addClickListener(event -> retryFailedScans());
        cancelScan.setText(message("inventory_health_view.cancel_scan"));
        cancelScan.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelScan.setVisible(startScan.isVisible());
        cancelScan.addClickListener(event -> cancelScan());
        export.setText(message("inventory_health_view.export_csv"));
        export.getElement().setAttribute("download", true);
        export.setHref(new StreamResource("inventory-health.csv", this::createCsv));
        HorizontalLayout header = new HorizontalLayout(title, scanStatus, computerFilter, staleDays, startScan, scanSelected, retryFailed, cancelScan, export, refresh);
        header.setWidthFull(); header.setAlignItems(Alignment.END); header.setFlexGrow(1, title);

        HorizontalLayout summary = new HorizontalLayout(metric(message("inventory_health_view.total"), total),
                metric(message("inventory_health_view.stale"), stale), metric(message("inventory_health_view.never_scanned"), neverScanned));
        summary.setWidthFull();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        grid.addColumn(InventoryHealthComputer::name).setHeader(message("inventory_health_view.name")).setAutoWidth(true);
        grid.addColumn(item -> item.checkingDate() == null ? message("inventory_health_view.never") : item.checkingDate().format(formatter))
                .setHeader(message("inventory_health_view.last_scan")).setAutoWidth(true);
        grid.addColumn(item -> item.daysSinceCheck() < 0 ? "-" : String.valueOf(item.daysSinceCheck()))
                .setHeader(message("inventory_health_view.days_since_scan")).setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Span status = new Span(item.daysSinceCheck() < 0 ? message("inventory_health_view.never") :
                    message("inventory_health_view.stale_status"));
            status.getElement().getThemeList().add("badge");
            status.getStyle().set("color", item.daysSinceCheck() < 0
                    ? "var(--lumo-secondary-text-color)" : "var(--lumo-warning-text-color)");
            return status;
        }).setHeader(message("inventory_health_view.status")).setAutoWidth(true);
        grid.addColumn(item -> item.scanStatus() == null ? "-" : item.scanStatus())
                .setHeader(message("inventory_health_view.scan_result")).setAutoWidth(true);
        grid.addColumn(item -> item.scanError() == null ? "" : item.scanError())
                .setHeader(message("inventory_health_view.scan_error_details")).setFlexGrow(1);
        grid.addComponentColumn(item -> {
            Button create = new Button(message("inventory_health_view.create_incident"));
            create.addClickListener(event -> confirmCreateIncident(item));
            return create;
        }).setHeader(message("inventory_health_view.actions")).setAutoWidth(true);
        grid.addItemClickListener(event -> {
            if (event.getItem().name() != null) {
                event.getSource().getUI().ifPresent(ui -> ui.navigate("management/computers/" + event.getItem().name() + "/details"));
            }
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.setSizeFull();
        historyGrid.addColumn(item -> item.startedAt() == null ? "-" : item.startedAt().format(formatter))
                .setHeader(message("inventory_health_view.scan_started_at")).setAutoWidth(true);
        historyGrid.addColumn(item -> item.finishedAt() == null ? "-" : item.finishedAt().format(formatter))
                .setHeader(message("inventory_health_view.scan_finished_at")).setAutoWidth(true);
        historyGrid.addColumn(InventoryScanRun::status)
                .setHeader(message("inventory_health_view.scan_status")).setAutoWidth(true);
        historyGrid.addColumn(item -> item.error() == null ? "" : item.error())
                .setHeader(message("inventory_health_view.scan_error_details")).setFlexGrow(1);
        historyGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        historyGrid.setWidthFull();
        historyGrid.setHeight("180px");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        add(header, summary, new H3(message("inventory_health_view.scan_history")), historyGrid, grid);
        expand(grid);
        refresh();
    }

    private void startScan() {
        if (inventoryService.startScan()) {
            Notification.show(message("inventory_health_view.scan_started"));
            updateScanStatus();
        } else {
            Notification notification = Notification.show(message("inventory_health_view.scan_running"));
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        }
    }

    private void startSelectedScan() {
        List<String> names = grid.getSelectedItems().stream().map(InventoryHealthComputer::name).toList();
        if (names.isEmpty()) {
            Notification notification = Notification.show(message("inventory_health_view.select_computers"));
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }
        if (inventoryService.startScan(names)) {
            grid.deselectAll();
            Notification.show(message("inventory_health_view.scan_started"));
            updateScanStatus();
        }
    }

    private void retryFailedScans() {
        List<String> names = currentComputers.stream()
                .filter(item -> "ERROR".equalsIgnoreCase(item.scanStatus()))
                .map(InventoryHealthComputer::name)
                .toList();
        if (names.isEmpty()) {
            Notification notification = Notification.show(message("inventory_health_view.no_failed_scans"));
            notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }
        if (inventoryService.startScan(names)) {
            Notification.show(message("inventory_health_view.retry_started"));
            updateScanStatus();
        }
    }

    private void cancelScan() {
        if (inventoryService.cancelScan()) {
            Notification.show(message("inventory_health_view.cancel_requested"));
            updateScanStatus();
        }
    }

    private HorizontalLayout metric(String label, Span value) {
        Span caption = new Span(label);
        caption.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        value.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "600");
        VerticalLayout content = new VerticalLayout(caption, value);
        content.setPadding(true); content.setSpacing(false);
        content.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        return new HorizontalLayout(content);
    }

    private void refresh() {
        InventoryHealthDto result = inventoryService.getInventoryHealth(staleDays.getValue() == null ? 30 : staleDays.getValue());
        if (result == null) {
            Notification notification = Notification.show(message("inventory_health_view.error"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            grid.setItems(List.of());
            return;
        }
        total.setText(String.valueOf(result.totalComputers()));
        stale.setText(String.valueOf(result.staleCount()));
        neverScanned.setText(String.valueOf(result.neverScannedCount()));
        currentComputers = result.computers() == null ? List.of() : result.computers();
        applyFilter();
        updateScanStatus();
        historyGrid.setItems(inventoryService.getScanHistory());
    }

    private void applyFilter() {
        String filter = computerFilter.getValue() == null ? "" : computerFilter.getValue().trim().toLowerCase();
        grid.setItems(currentComputers.stream()
                .filter(item -> filter.isBlank() || item.name() != null && item.name().toLowerCase().contains(filter))
                .toList());
    }

    private ByteArrayInputStream createCsv() {
        StringBuilder csv = new StringBuilder("Computer,Last scan,Days since scan,Status\n");
        currentComputers.forEach(item -> csv.append(csvValue(item.name())).append(',')
                .append(csvValue(item.checkingDate() == null ? "" : item.checkingDate().toString())).append(',')
                .append(item.daysSinceCheck() < 0 ? "" : item.daysSinceCheck()).append(',')
                .append(csvValue(item.daysSinceCheck() < 0 ? message("inventory_health_view.never") :
                        message("inventory_health_view.stale_status"))).append('\n'));
        return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String csvValue(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void updateScanStatus() {
        var status = inventoryService.getScanStatus();
        if (status == null) {
            scanStatus.setText("");
        } else if (status.running()) {
            scanStatus.setText(message("inventory_health_view.scan_running") + " ("
                    + status.processed() + "/" + status.total() + ")");
        } else if (status.lastError() != null) {
            scanStatus.setText(message("inventory_health_view.scan_error"));
        } else if (status.finishedAt() != null) {
            scanStatus.setText(message("inventory_health_view.scan_finished"));
        } else {
            scanStatus.setText(message("inventory_health_view.scan_idle"));
        }
        cancelScan.setVisible(status != null && status.running()
                && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        scanStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
    }

    private void confirmCreateIncident(InventoryHealthComputer computer) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(message("inventory_health_view.create_incident"));
        dialog.setText(message("inventory_health_view.create_incident_confirm"));
        dialog.setCancelable(true);
        dialog.setConfirmText(message("inventory_health_view.create_incident"));
        dialog.addConfirmListener(event -> {
            try {
                IncidentItem incident = new IncidentItem();
                incident.setSignalId("INVENTORY_STALE");
                incident.setName("Inventory scan overdue: " + computer.name());
                incident.setSeverity(Severity.MEDIUM);
                incident.setStatus(IncidentStatus.OPEN);
                incident.setEventCount(1);
                incident.setRecommendation("Run an inventory scan and check the computer connectivity.");
                incident.setContext("Last scan: " + (computer.checkingDate() == null ? "never" : computer.checkingDate()));
                incident.setMachineName(computer.name());
                incident.setMeta(false);
                incident.setCreatedAt(LocalDateTime.now());
                incident.setDeduplicationKey("manual-inventory-" + UUID.randomUUID());
                incidentService.createIncident(incident);
                Notification.show(message("inventory_health_view.incident_created"));
            } catch (Exception exception) {
                Notification notification = Notification.show(exception.getMessage() == null ? message("common.error") : exception.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private String message(String key) { return messageSource.getMessage(key, null, localeService.getCurrentLocale()); }
    @Override public String getPageTitle() { return message("inventory_health_view.title"); }
}
