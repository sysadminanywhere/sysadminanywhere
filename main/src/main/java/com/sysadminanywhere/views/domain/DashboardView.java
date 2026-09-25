package com.sysadminanywhere.views.domain;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.common.directory.model.*;
import com.sysadminanywhere.service.*;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.format.DateTimeFormatter;

@Route(value = "")
@RouteAlias(value = "domain/dashboard")
@RouteAlias(value = "dashboard")
@RolesAllowed({"ADMIN", "READER"})
public class DashboardView extends VerticalLayout implements HasDynamicTitle {
    private static final Logger log = LoggerFactory.getLogger(DashboardView.class);

    private final String ColumnWidth = "55%";
    private final String ChartHeight = "300px";
    private final String ChartWidth = "400px";

    private final ComputersService computersService;
    private final UsersService usersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;
    private final ContactsService contactsService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final InventoryService inventoryService;
    private final DashboardSnapshotService dashboardSnapshotService;
    private final VerticalLayout content = new VerticalLayout();
    private final Span loading = new Span();
    private boolean loadingStarted;
    private long loadGeneration;

    private final String title = "dashboard_view.title";

    public DashboardView(ComputersService computersService,
                         UsersService usersService,
                         GroupsService groupsService,
                         PrintersService printersService,
                         ContactsService contactsService,
                         InventoryService inventoryService,
                         DashboardSnapshotService dashboardSnapshotService,
                         MessageSource messageSource,
                         LocaleService localeService) {

        this.computersService = computersService;
        this.usersService = usersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
        this.contactsService = contactsService;
        this.inventoryService = inventoryService;
        this.dashboardSnapshotService = dashboardSnapshotService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setWidthFull();
        content.setWidthFull();
        content.setPadding(false);
        loading.setText(getMessage("dashboard_view.loading"));
        Button refresh = new Button(getMessage("common.refresh"), event -> {
            dashboardSnapshotService.invalidate();
            loadingStarted = false;
            loadDashboard();
        });
        add(refresh, loading, content);
        content.add(createLoadingCards());
        getElement().executeJs("requestAnimationFrame(() => $0.$server.loadDashboard())", getElement());
    }

    private HorizontalLayout createLoadingCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setWrap(true);
        cards.add(metricCard(getMessage("domain_health_view.title"), "unknown", "…", Map.of()),
                metricCard(getMessage("security_audit_view.title"), "unknown", "…", Map.of()),
                metricCard(getMessage("dashboard_view.users"), "unknown", "…", Map.of()),
                metricCard(getMessage("inventory_health_view.title"), "unknown", "…", Map.of()));
        return cards;
    }

    @ClientCallable
    public void loadDashboard() {
        if (loadingStarted) return;
        loadingStarted = true;
        long generation = ++loadGeneration;
        loading.setText(getMessage("dashboard_view.loading"));
        loading.setVisible(true);
        long startedAt = System.nanoTime();
        try {
            List<ComputerEntry> computers = computersService.getAll();
            List<UserEntry> users = usersService.getAll();
            List<GroupEntry> groups = groupsService.getAll();
            List<PrinterEntry> printers = printersService.getAll();
            List<ContactEntry> contacts = contactsService.getAll();
            long directoryMs = (System.nanoTime() - startedAt) / 1_000_000;

            HorizontalLayout overview = createOverviewCards(users, computers);
            long overviewMs = (System.nanoTime() - startedAt) / 1_000_000 - directoryMs;
            log.info("Dashboard data loaded: directory={} ms, overview={} ms", directoryMs, overviewMs);
            content.removeAll();
            content.add(overview);
            loading.setVisible(false);

            getStoredTheme().thenAccept(v -> {
            if (generation != loadGeneration || !isAttached()) return;
            boolean isDarkTheme = v.contains("dark");

            String theme = isDarkTheme ? "dark" : "light";
            String foreColor = isDarkTheme ? "white" : "black";

            ApexCharts chartSummary = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
                                    .build())
                            .build())
                    .withDataLabels(DataLabelsBuilder.get()
                            .withEnabled(false).build())
                    .withStroke(StrokeBuilder.get()
                            .withShow(true)
                            .withWidth(2.0)
                            .withColors("transparent")
                            .build())
                    .withSeries(new Series<>(getMessage("dashboard_view.computers"), computers.size()),
                            new Series<>(getMessage("dashboard_view.users"), users.size()),
                            new Series<>(getMessage("dashboard_view.groups"), groups.size()),
                            new Series<>(getMessage("main_layout.printers"), printers.size()),
                            new Series<>(getMessage("main_layout.contacts"), contacts.size()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartSummary.setHeight(ChartHeight);
            chartSummary.setWidth(ChartWidth);
            chartSummary.setTitle(TitleSubtitleBuilder.get().withText(getMessage("dashboard_view.summary")).build());

            ApexCharts chartUsers = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
                                    .build())
                            .build())
                    .withDataLabels(DataLabelsBuilder.get()
                            .withEnabled(false).build())
                    .withStroke(StrokeBuilder.get()
                            .withShow(true)
                            .withWidth(2.0)
                            .withColors("transparent")
                            .build())
                    .withSeries(new Series<>(getMessage("dashboard_view.number_of_users"), users.size()),
                            new Series<>(getMessage("common.disabled"), users.stream().filter(c -> c.isDisabled()).count()),
                            new Series<>(getMessage("common.locked"), users.stream().filter(c -> c.isLocked()).count()),
                            new Series<>(getMessage("common.expired"), users.stream().filter(c -> c.isExpired()).count()),
                            new Series<>(getMessage("common.never_expires"), users.stream().filter(c -> c.isNeverExpires()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartUsers.setHeight(ChartHeight);
            chartUsers.setWidth(ChartWidth);
            chartUsers.setTitle(TitleSubtitleBuilder.get().withText(getMessage("dashboard_view.users")).build());

            ApexCharts chartComputers = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
                                    .build())
                            .build())
                    .withDataLabels(DataLabelsBuilder.get()
                            .withEnabled(false).build())
                    .withStroke(StrokeBuilder.get()
                            .withShow(true)
                            .withWidth(2.0)
                            .withColors("transparent")
                            .build())
                    .withSeries(new Series<>(getMessage("dashboard_view.number_of_computers"), computers.size()),
                            new Series<>(getMessage("common.disabled"), computers.stream().filter(c -> c.isDisabled()).count()),
                            new Series<>(getMessage("dashboard_view.workstations"), computers.stream().filter(c -> c.isWorkstation()).count()),
                            new Series<>(getMessage("dashboard_view.servers"), computers.stream().filter(c -> c.isServer()).count()),
                            new Series<>(getMessage("dashboard_view.domain_controllers"), computers.stream().filter(c -> c.isDomainController()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartComputers.setHeight(ChartHeight);
            chartComputers.setWidth(ChartWidth);
            chartComputers.setTitle(TitleSubtitleBuilder.get().withText(getMessage("dashboard_view.computers")).build());

            ApexCharts chartGroups = ApexChartsBuilder.get()
                    .withTooltip(TooltipBuilder.get()
                            .withFillSeriesColor(false)
                            .withTheme(theme).build())
                    .withChart(ChartBuilder.get()
                            .withType(Type.BAR)
                            .withForeColor(foreColor)
                            .withToolbar(ToolbarBuilder.get().withShow(false).build())
                            .build())
                    .withPlotOptions(PlotOptionsBuilder.get()
                            .withBar(BarBuilder.get()
                                    .withHorizontal(false)
                                    .withColumnWidth(ColumnWidth)
                                    .build())
                            .build())
                    .withDataLabels(DataLabelsBuilder.get()
                            .withEnabled(false).build())
                    .withStroke(StrokeBuilder.get()
                            .withShow(true)
                            .withWidth(2.0)
                            .withColors("transparent")
                            .build())
                    .withSeries(new Series<>(getMessage("dashboard_view.number_of_groups"), groups.size()),
                            new Series<>(getMessage("dashboard_view.security"), groups.stream().filter(c -> c.isSecurity()).count()),
                            new Series<>(getMessage("dashboard_view.distribution"), groups.stream().filter(c -> c.isDistribution()).count()),
                            new Series<>(getMessage("dashboard_view.built_in"), groups.stream().filter(c -> c.isBuiltIn()).count()))
                    .withXaxis(XAxisBuilder.get().withCategories("Count").build())
                    .withFill(FillBuilder.get().withOpacity(1.0).build()).build();
            chartGroups.setHeight(ChartHeight);
            chartGroups.setWidth(ChartWidth);
            chartGroups.setTitle(TitleSubtitleBuilder.get().withText(getMessage("dashboard_view.groups")).build());

            HorizontalLayout line = new HorizontalLayout();
            line.setWrap(true);
            line.add(chartSummary, chartUsers, chartComputers, chartGroups);

            content.add(line);

            });
        } catch (RuntimeException exception) {
            log.warn("Dashboard loading failed", exception);
            loading.setText(getMessage("domain_health_view.error"));
            loadingStarted = false;
        }
    }

    private HorizontalLayout createOverviewCards(List<UserEntry> users, List<ComputerEntry> computers) {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setWrap(true);
        cards.setSpacing(true);

        var domainHealth = dashboardSnapshotService.domainHealth();
        String healthStatus = domainHealth == null || domainHealth.getOverallStatus() == null
                ? "UNKNOWN" : domainHealth.getOverallStatus();
        Card domainCard = metricCard(getMessage("domain_health_view.title"), healthClass(healthStatus),
                localizedHealthStatus(healthStatus), Map.of(
                getMessage("domain_health_view.overall"), localizedHealthStatus(healthStatus),
                getMessage("domain_health_view.check"), domainHealth == null || domainHealth.getChecks() == null
                        ? 0 : domainHealth.getChecks().size()));
        if (domainHealth != null && domainHealth.getCheckedAt() != null) {
            addCheckedAt(domainCard, domainHealth.getCheckedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        addDetailsLink(domainCard, "domain/info");

        var security = dashboardSnapshotService.securityAudit();
        int securityIssues = security.privilegedUsers() + security.privilegedGroups()
                + security.usersMissingContactData();
        boolean securityFailed = security.error() != null;
        Card securityCard = metricCard(getMessage("security_audit_view.title"), securityFailed ? "error" : securityIssues == 0 ? "ok" : "warning",
                securityFailed ? getMessage("domain_health_view.error") : securityIssues == 0 ? getMessage("domain_health_view.healthy") : getMessage("domain_health_view.warning"), Map.of(
                getMessage("security_audit_view.privileged_users"), security.privilegedUsers(),
                getMessage("security_audit_view.privileged_groups"), security.privilegedGroups(),
                getMessage("security_audit_view.missing_contact"), security.usersMissingContactData()));
        if (security.checkedAt() != null) {
            addCheckedAt(securityCard, security.checkedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        addDetailsLink(securityCard, "security/audit");

        long accountIssues = users.stream().filter(user -> user.isDisabled() || user.isLocked() || user.isExpired()).count();
        Card accountsCard = metricCard(getMessage("dashboard_view.users"), accountIssues == 0 ? "ok" : "warning",
                accountIssues == 0 ? getMessage("domain_health_view.healthy") : getMessage("domain_health_view.warning"), Map.of(
                getMessage("common.disabled"), users.stream().filter(UserEntry::isDisabled).count(),
                getMessage("common.locked"), users.stream().filter(UserEntry::isLocked).count(),
                getMessage("common.expired"), users.stream().filter(UserEntry::isExpired).count()));
        addDetailsLink(accountsCard, "management/users");

        var inventory = inventoryService.getInventoryHealth(30);
        long licenseIssues = inventoryService.getLicenses().stream()
                .filter(license -> license.used() > license.purchased()
                        || license.expiresAt() != null && license.expiresAt().isBefore(java.time.LocalDate.now()))
                .count();
        int inventoryIssues = inventory == null ? -1 : inventory.staleCount() + inventory.neverScannedCount() + (int) licenseIssues;
        Card inventoryCard = metricCard(getMessage("inventory_health_view.title"), inventoryIssues < 0 ? "error" :
                        inventoryIssues == 0 ? "ok" : "warning",
                inventoryIssues < 0 ? getMessage("domain_health_view.error") : inventoryIssues == 0 ?
                        getMessage("domain_health_view.healthy") : getMessage("domain_health_view.warning"), Map.of(
                getMessage("inventory_health_view.total"), inventory == null ? 0 : inventory.totalComputers(),
                getMessage("inventory_health_view.stale"), inventory == null ? 0 : inventory.staleCount(),
                getMessage("inventory_health_view.never_scanned"), inventory == null ? 0 : inventory.neverScannedCount(),
                getMessage("dashboard_view.license_issues"), licenseIssues));
        addDetailsLink(inventoryCard, "inventory/health");

        cards.add(domainCard, securityCard, accountsCard, inventoryCard);
        return cards;
    }

    private Card metricCard(String title, String statusClass, String statusText, Map<String, ?> values) {
        Card card = new Card();
        card.addClassName("overview-card");
        card.addClassName("overview-" + statusClass);
        card.setWidth("min(100%, 360px)");
        card.getStyle().set("flex", "1 1 240px");
        card.setTitle(title);
        Span status = new Span(statusText);
        status.addClassName("overview-status-badge");
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setWidthFull();
        content.getStyle().set("flex", "1 1 auto");
        values.forEach((label, value) -> {
            HorizontalLayout row = new HorizontalLayout(new Span(label), new H3(String.valueOf(value)));
            row.setWidthFull();
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            content.add(row);
        });
        card.add(status, content);
        return card;
    }

    private void addDetailsLink(Card card, String route) {
        Anchor details = new Anchor(route, getMessage("common.details"));
        details.addClassName("overview-details-link");
        card.add(details);
    }

    private void addCheckedAt(Card card, String value) {
        Span checkedAt = new Span(getMessage("domain_health_view.checked_at") + ": " + value);
        checkedAt.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        card.add(checkedAt);
    }

    private String healthClass(String value) {
        return switch (value) {
            case "HEALTHY" -> "ok";
            case "WARNING" -> "warning";
            case "ERROR" -> "error";
            default -> "unknown";
        };
    }

    private String localizedHealthStatus(String value) {
        return switch (value) {
            case "HEALTHY" -> getMessage("domain_health_view.healthy");
            case "WARNING" -> getMessage("domain_health_view.warning");
            case "ERROR" -> getMessage("domain_health_view.error");
            default -> getMessage("domain_health_view.not_checked");
        };
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    private CompletableFuture<String> getStoredTheme() {
        CompletableFuture<String> future = new CompletableFuture<>();

        UI.getCurrent().getPage().executeJs("return localStorage.getItem('theme');")
                .then(String.class, theme -> {
                    future.complete(theme != null ? theme : "light");
                });

        return future;
    }

    @Override
    public String getPageTitle() {
        return getMessage(title);
    }

}
