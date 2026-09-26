package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.common.directory.dto.DomainHealthDto;
import com.sysadminanywhere.model.SecurityAuditSnapshot;
import com.sysadminanywhere.service.DashboardDirectorySnapshotService;
import com.sysadminanywhere.service.DashboardSnapshotService;
import com.sysadminanywhere.service.InventoryService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

@Route(value = "")
@RouteAlias(value = "domain/dashboard")
@RouteAlias(value = "dashboard")
@RolesAllowed({"ADMIN", "READER"})
public class DashboardView extends VerticalLayout implements HasDynamicTitle {
    private static final Logger log = LoggerFactory.getLogger(DashboardView.class);
    private static final DateTimeFormatter CHECKED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DashboardDirectorySnapshotService directorySnapshots;
    private final DashboardSnapshotService healthSnapshots;
    private final InventoryService inventoryService;
    private final MessageSource messages;
    private final LocaleService locale;
    private final Div content = new Div();
    private final Span loading = new Span();
    private boolean loadingStarted;
    private int loadGeneration;
    private Div statusContainer;

    public DashboardView(DashboardDirectorySnapshotService directorySnapshots,
                         DashboardSnapshotService healthSnapshots,
                         InventoryService inventoryService, MessageSource messages, LocaleService locale) {
        this.directorySnapshots = directorySnapshots;
        this.healthSnapshots = healthSnapshots;
        this.inventoryService = inventoryService;
        this.messages = messages;
        this.locale = locale;

        addClassName("dashboard-page");
        setWidthFull();
        setPadding(false);

        H2 title = new H2(msg("dashboard_view.title"));
        title.addClassName("dashboard-title");
        Button refresh = new Button(msg("common.refresh"), event -> {
            directorySnapshots.invalidate();
            healthSnapshots.invalidate();
            ++loadGeneration;
            loadingStarted = false;
            loading.setText(msg("dashboard_view.loading"));
            loading.setVisible(true);
            content.removeAll();
            content.add(skeletonCards());
            getElement().executeJs("requestAnimationFrame(() => $0.$server.loadDashboard())", getElement());
        });
        refresh.addClassName("dashboard-refresh");
        Div header = new Div(title, refresh);
        header.addClassName("dashboard-header");

        loading.setText(msg("dashboard_view.loading"));
        loading.addClassName("dashboard-loading");
        content.addClassName("dashboard-content");
        content.add(skeletonCards());
        add(header, loading, content);
        getElement().executeJs("requestAnimationFrame(() => $0.$server.loadDashboard())", getElement());
    }

    @ClientCallable
    public void loadDashboard() {
        if (loadingStarted) return;
        loadingStarted = true;
        int generation = ++loadGeneration;
        loading.setText(msg("dashboard_view.loading"));
        loading.setVisible(true);
        long startedAt = System.nanoTime();
        try {
            DashboardDirectorySnapshotService.Snapshot directory = directorySnapshots.get();
            content.removeAll();
            statusContainer = statusSkeleton();
            content.add(directoryCards(directory), statusContainer, charts(directory));
            loading.setVisible(false);
            log.info("Dashboard overview rendered in {} ms", (System.nanoTime() - startedAt) / 1_000_000);
            getElement().executeJs("requestAnimationFrame(() => $0.$server.loadStatus($1))", getElement(), generation);
        } catch (RuntimeException exception) {
            log.warn("Dashboard loading failed", exception);
            loading.setText(msg("domain_health_view.error"));
            loadingStarted = false;
        }
    }

    @ClientCallable
    public void loadStatus(int generation) {
        if (generation != loadGeneration || statusContainer == null || !isAttached()) return;
        try {
            statusContainer.removeAll();
            statusContainer.add(statusCards());
        } catch (RuntimeException exception) {
            log.warn("Dashboard status loading failed", exception);
            statusContainer.removeAll();
            statusContainer.add(new Span(msg("domain_health_view.error")));
        }
    }

    private Div statusSkeleton() {
        Div container = new Div();
        container.addClassName("dashboard-status-container");
        Div section = section(msg("dashboard_view.status_section"));
        Div grid = new Div();
        grid.addClassName("dashboard-status-grid");
        for (int i = 0; i < 5; i++) {
            Div placeholder = new Div();
            placeholder.addClassName("dashboard-skeleton");
            grid.add(placeholder);
        }
        section.add(grid);
        container.add(section);
        return container;
    }

    private Div skeletonCards() {
        Div grid = new Div();
        grid.addClassName("dashboard-metrics");
        for (int i = 0; i < 5; i++) {
            Div placeholder = new Div();
            placeholder.addClassName("dashboard-skeleton");
            grid.add(placeholder);
        }
        return grid;
    }

    private Div directoryCards(DashboardDirectorySnapshotService.Snapshot data) {
        Div section = section(msg("dashboard_view.summary"));
        Div grid = new Div();
        grid.addClassName("dashboard-metrics");
        grid.add(metric(msg("dashboard_view.users"), data.users().size(), "management/users",
                        msg("common.disabled") + ": " + count(data.users(), UserEntry::isDisabled)),
                metric(msg("dashboard_view.computers"), data.computers().size(), "management/computers",
                        msg("common.disabled") + ": " + count(data.computers(), ComputerEntry::isDisabled)),
                metric(msg("dashboard_view.groups"), data.groups().size(), "management/groups",
                        msg("dashboard_view.security") + ": " + count(data.groups(), GroupEntry::isSecurity)),
                metric(msg("main_layout.printers"), data.printers(), "management/printers", null),
                metric(msg("main_layout.contacts"), data.contacts(), "management/contacts", null));
        section.add(grid);
        return section;
    }

    private Div metric(String label, long value, String route, String detail) {
        Anchor card = new Anchor(route);
        card.addClassName("dashboard-metric");
        Span name = new Span(label);
        name.addClassName("dashboard-metric-label");
        Span count = new Span(Long.toString(value));
        count.addClassName("dashboard-metric-value");
        card.add(name, count);
        if (detail != null) {
            Span note = new Span(detail);
            note.addClassName("dashboard-metric-note");
            card.add(note);
        }
        return new Div(card);
    }

    private Div statusCards() {
        Div section = section(msg("dashboard_view.status_section"));
        Div grid = new Div();
        grid.addClassName("dashboard-status-grid");

        DomainHealthDto domain;
        try {
            domain = healthSnapshots.domainHealth();
        } catch (RuntimeException exception) {
            log.warn("Domain health is unavailable", exception);
            domain = null;
        }
        String domainStatus = domain == null || domain.getOverallStatus() == null ? "UNKNOWN" : domain.getOverallStatus();
        Div domainCard = statusCard(msg("domain_health_view.title"), healthClass(domainStatus),
                localizedHealth(domainStatus), "domain/info");
        statusRow(domainCard, msg("domain_health_view.check"), domain == null || domain.getChecks() == null ? 0 : domain.getChecks().size());
        if (domain != null && domain.getCheckedAt() != null) {
            statusFootnote(domainCard, msg("domain_health_view.checked_at") + ": " + domain.getCheckedAt().format(CHECKED_AT));
        }

        SecurityAuditSnapshot audit;
        try {
            audit = healthSnapshots.securityAudit();
        } catch (RuntimeException exception) {
            log.warn("Security audit is unavailable", exception);
            audit = null;
        }
        boolean auditError = audit == null || audit.error() != null;
        long weakSettings = auditError ? 0 : audit.findings().stream()
                .filter(finding -> "WEAK_AUTHENTICATION".equals(finding.category())).count();
        long auditIssues = auditError ? 0 : audit.usersMissingContactData() + weakSettings;
        Div securityCard = statusCard(msg("security_audit_view.title"), auditError ? "error" : auditIssues > 0 ? "warning" : "ok",
                auditError ? msg("domain_health_view.error") : auditIssues > 0 ? msg("domain_health_view.warning") : msg("domain_health_view.healthy"),
                "security/audit");
        if (!auditError) {
            statusRow(securityCard, msg("security_audit_view.missing_contact"), audit.usersMissingContactData());
            statusRow(securityCard, msg("dashboard_view.weak_settings"), weakSettings);
            statusRow(securityCard, msg("security_audit_view.privileged_users"), audit.privilegedUsers());
        }

        DashboardDirectorySnapshotService.Snapshot data = directorySnapshots.get();
        long accountIssues = data.users().stream().filter(user -> user.isDisabled() || user.isLocked() || user.isExpired()).count();
        Div accountsCard = statusCard(msg("dashboard_view.users"), accountIssues > 0 ? "warning" : "ok",
                accountIssues > 0 ? msg("domain_health_view.warning") : msg("domain_health_view.healthy"), "management/users");
        statusRow(accountsCard, msg("common.disabled"), count(data.users(), UserEntry::isDisabled));
        statusRow(accountsCard, msg("common.locked"), count(data.users(), UserEntry::isLocked));
        statusRow(accountsCard, msg("common.expired"), count(data.users(), UserEntry::isExpired));

        var inventory = inventoryService.getInventoryHealth(30);
        int inventoryIssues = inventory == null ? -1 : inventory.staleCount() + inventory.neverScannedCount();
        Div inventoryCard = statusCard(msg("inventory_health_view.title"), inventoryIssues < 0 ? "error" : inventoryIssues > 0 ? "warning" : "ok",
                inventoryIssues < 0 ? msg("domain_health_view.error") : inventoryIssues > 0 ? msg("domain_health_view.warning") : msg("domain_health_view.healthy"),
                "inventory/health");
        if (inventory != null) {
            statusRow(inventoryCard, msg("inventory_health_view.total"), inventory.totalComputers());
            statusRow(inventoryCard, msg("inventory_health_view.stale"), inventory.staleCount());
            statusRow(inventoryCard, msg("inventory_health_view.never_scanned"), inventory.neverScannedCount());
        }

        var licenses = inventoryService.getLicenses();
        long licenseIssues = licenses.stream().filter(license -> license.used() > license.purchased()
                || license.expiresAt() != null && license.expiresAt().isBefore(LocalDate.now())).count();
        Div licenseCard = statusCard(msg("inventory_licenses_view.title"), licenseIssues > 0 ? "warning" : "ok",
                licenseIssues > 0 ? msg("domain_health_view.warning") : msg("domain_health_view.healthy"), "inventory/licenses");
        statusRow(licenseCard, msg("dashboard_view.license_issues"), licenseIssues);
        statusRow(licenseCard, msg("dashboard_view.license_total"), licenses.size());

        grid.add(domainCard, securityCard, accountsCard, inventoryCard, licenseCard);
        section.add(grid);
        return section;
    }

    private Div charts(DashboardDirectorySnapshotService.Snapshot data) {
        Div section = section(msg("dashboard_view.distribution"));
        Div grid = new Div();
        grid.addClassName("dashboard-chart-grid");
        grid.add(chart(msg("dashboard_view.users"), data.users().size(),
                        part(msg("common.disabled"), count(data.users(), UserEntry::isDisabled), "warning"),
                        part(msg("common.locked"), count(data.users(), UserEntry::isLocked), "error"),
                        part(msg("common.expired"), count(data.users(), UserEntry::isExpired), "error"),
                        part(msg("common.never_expires"), count(data.users(), UserEntry::isNeverExpires), "primary")),
                chart(msg("dashboard_view.computers"), data.computers().size(),
                        part(msg("dashboard_view.workstations"), count(data.computers(), ComputerEntry::isWorkstation), "primary"),
                        part(msg("dashboard_view.servers"), count(data.computers(), ComputerEntry::isServer), "teal"),
                        part(msg("dashboard_view.domain_controllers"), count(data.computers(), ComputerEntry::isDomainController), "violet"),
                        part(msg("common.disabled"), count(data.computers(), ComputerEntry::isDisabled), "warning")),
                chart(msg("dashboard_view.groups"), data.groups().size(),
                        part(msg("dashboard_view.security"), count(data.groups(), GroupEntry::isSecurity), "primary"),
                        part(msg("dashboard_view.distribution"), count(data.groups(), GroupEntry::isDistribution), "teal"),
                        part(msg("dashboard_view.built_in"), count(data.groups(), GroupEntry::isBuiltIn), "violet")));
        section.add(grid);
        return section;
    }

    private Div chart(String title, long total, ChartPart... parts) {
        Div panel = new Div();
        panel.addClassName("dashboard-chart");
        H3 heading = new H3(title);
        heading.addClassName("dashboard-chart-title");
        panel.add(heading);
        for (ChartPart part : parts) {
            Div row = new Div();
            row.addClassName("dashboard-chart-row");
            Span label = new Span(part.label());
            Span value = new Span(Long.toString(part.value()));
            Div track = new Div();
            track.addClassName("dashboard-chart-track");
            Div fill = new Div();
            fill.addClassNames("dashboard-chart-fill", "dashboard-chart-" + part.tone());
            fill.getStyle().set("width", total == 0 ? "0%" : Math.min(100, Math.round(part.value() * 100.0 / total)) + "%");
            track.add(fill);
            row.add(label, value, track);
            panel.add(row);
        }
        return panel;
    }

    private ChartPart part(String label, long value, String tone) {
        return new ChartPart(label, value, tone);
    }

    private record ChartPart(String label, long value, String tone) {
    }

    private Div section(String title) {
        Div section = new Div();
        section.addClassName("dashboard-section");
        H3 heading = new H3(title);
        heading.addClassName("dashboard-section-title");
        section.add(heading);
        return section;
    }

    private Div statusCard(String title, String tone, String status, String route) {
        Div card = new Div();
        card.addClassNames("dashboard-status-card", "dashboard-status-" + tone);
        Div heading = new Div();
        heading.addClassName("dashboard-status-heading");
        H3 name = new H3(title);
        Span badge = new Span(status);
        badge.addClassName("dashboard-status-badge");
        heading.add(name, badge);
        card.add(heading);
        Anchor details = new Anchor(route, msg("common.details") + " →");
        details.addClassName("dashboard-status-link");
        card.add(details);
        return card;
    }

    private void statusRow(Div card, String label, long value) {
        Div row = new Div(new Span(label), new Span(Long.toString(value)));
        row.addClassName("dashboard-status-row");
        card.addComponentAtIndex(card.getComponentCount() - 1, row);
    }

    private void statusFootnote(Div card, String text) {
        Span note = new Span(text);
        note.addClassName("dashboard-status-note");
        card.addComponentAtIndex(card.getComponentCount() - 1, note);
    }

    private String healthClass(String status) {
        return switch (status) {
            case "HEALTHY" -> "ok";
            case "WARNING" -> "warning";
            case "ERROR" -> "error";
            default -> "unknown";
        };
    }

    private String localizedHealth(String status) {
        return switch (status) {
            case "HEALTHY" -> msg("domain_health_view.healthy");
            case "WARNING" -> msg("domain_health_view.warning");
            case "ERROR" -> msg("domain_health_view.error");
            default -> msg("domain_health_view.not_checked");
        };
    }

    private <T> long count(List<T> items, Predicate<T> predicate) {
        return items.stream().filter(predicate).count();
    }

    private String msg(String key) {
        return messages.getMessage(key, null, locale.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return msg("dashboard_view.title");
    }
}
