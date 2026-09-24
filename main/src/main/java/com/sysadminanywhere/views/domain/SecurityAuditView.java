package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.model.SecurityAuditSnapshot;
import com.sysadminanywhere.model.SecurityFinding;
import com.sysadminanywhere.service.IncidentService;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.SecurityAuditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import com.sysadminanywhere.common.incident.model.IncidentItem;
import com.sysadminanywhere.common.incident.model.IncidentStatus;
import com.sysadminanywhere.common.incident.model.Severity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "security/audit")
public class SecurityAuditView extends VerticalLayout implements HasDynamicTitle {
    private final SecurityAuditService securityAuditService;
    private final IncidentService incidentService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final Span privilegedUsers = new Span();
    private final Span privilegedGroups = new Span();
    private final Span userSpn = new Span();
    private final Span computerSpn = new Span();
    private final Span hygiene = new Span();
    private final Span checkedAt = new Span();
    private final Grid<SecurityFinding> findings = new Grid<>();

    public SecurityAuditView(SecurityAuditService securityAuditService, IncidentService incidentService,
                             MessageSource messageSource, LocaleService localeService) {
        this.securityAuditService = securityAuditService;
        this.incidentService = incidentService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(message("security_audit_view.title"));
        Button refresh = new Button(message("common.refresh"), event -> refresh());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout header = new HorizontalLayout(title, refresh);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, title);

        HorizontalLayout summary = new HorizontalLayout(metric(message("security_audit_view.privileged_users"), privilegedUsers),
                metric(message("security_audit_view.privileged_groups"), privilegedGroups),
                metric(message("security_audit_view.user_spn"), userSpn),
                metric(message("security_audit_view.computer_spn"), computerSpn),
                metric(message("security_audit_view.missing_contact"), hygiene));
        summary.setWidthFull();
        summary.setFlexGrow(1, summary.getComponentAt(0), summary.getComponentAt(1), summary.getComponentAt(2), summary.getComponentAt(3), summary.getComponentAt(4));

        findings.addColumn(SecurityFinding::category).setHeader(message("security_audit_view.category")).setAutoWidth(true);
        findings.addComponentColumn(item -> badge(item.severity())).setHeader(message("security_audit_view.severity")).setAutoWidth(true);
        findings.addColumn(SecurityFinding::objectType).setHeader(message("security_audit_view.object_type")).setAutoWidth(true);
        findings.addColumn(SecurityFinding::name).setHeader(message("security_audit_view.name")).setAutoWidth(true);
        findings.addColumn(SecurityFinding::distinguishedName).setHeader(message("security_audit_view.distinguished_name")).setFlexGrow(1);
        findings.addColumn(SecurityFinding::details).setHeader(message("security_audit_view.details")).setFlexGrow(1);
        findings.addComponentColumn(item -> {
            Button create = new Button(message("security_audit_view.create_incident"));
            create.addClickListener(event -> confirmCreateIncident(item));
            return create;
        }).setHeader(message("security_audit_view.actions")).setAutoWidth(true);
        findings.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        findings.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        findings.setSizeFull();

        checkedAt.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(header, summary, checkedAt, findings);
        expand(findings);
        refresh();
    }

    private HorizontalLayout metric(String label, Span value) {
        Span caption = new Span(label);
        caption.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");
        value.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "600");
        VerticalLayout content = new VerticalLayout(caption, value);
        content.setPadding(true);
        content.setSpacing(false);
        content.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.MEDIUM);
        return new HorizontalLayout(content);
    }

    private Span badge(String severity) {
        Span badge = new Span(severity == null ? "" : severity);
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add("HIGH".equals(severity) ? "error" :
                "WARNING".equals(severity) ? "warning" : "success");
        return badge;
    }

    private void refresh() {
        SecurityAuditSnapshot snapshot = securityAuditService.scan();
        privilegedUsers.setText(String.valueOf(snapshot.privilegedUsers()));
        privilegedGroups.setText(String.valueOf(snapshot.privilegedGroups()));
        userSpn.setText(String.valueOf(snapshot.usersWithSpn()));
        computerSpn.setText(String.valueOf(snapshot.computersWithSpn()));
        hygiene.setText(String.valueOf(snapshot.usersMissingContactData()));
        checkedAt.setText(message("security_audit_view.checked_at") + ": " + snapshot.checkedAt());
        findings.setItems(snapshot.findings() == null ? List.of() : snapshot.findings());
        if (snapshot.error() != null) {
            Notification notification = Notification.show(message("security_audit_view.error") + ": " + snapshot.error());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmCreateIncident(SecurityFinding finding) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(message("security_audit_view.create_incident"));
        dialog.setText(message("security_audit_view.create_incident_confirm"));
        dialog.setCancelable(true);
        dialog.setConfirmText(message("security_audit_view.create_incident"));
        dialog.addConfirmListener(event -> {
            try {
                IncidentItem incident = new IncidentItem();
                incident.setSignalId("SECURITY_" + finding.category());
                incident.setName("Security audit: " + finding.category());
                incident.setSeverity(toSeverity(finding.severity()));
                incident.setStatus(IncidentStatus.OPEN);
                incident.setEventCount(1);
                incident.setRecommendation(finding.details());
                incident.setContext(finding.distinguishedName());
                incident.setAffectedUser("USER".equals(finding.objectType()) ? finding.name() : null);
                incident.setMachineName("COMPUTER".equals(finding.objectType()) ? finding.name() : null);
                incident.setMeta(false);
                incident.setCreatedAt(LocalDateTime.now());
                incident.setDeduplicationKey("manual-security-" + UUID.randomUUID());
                incidentService.createIncident(incident);
                Notification.show(message("security_audit_view.incident_created"));
            } catch (Exception exception) {
                Notification notification = Notification.show(exception.getMessage() == null ? message("common.error") : exception.getMessage());
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private Severity toSeverity(String severity) {
        return switch (severity == null ? "" : severity) {
            case "HIGH" -> Severity.HIGH;
            case "WARNING" -> Severity.MEDIUM;
            default -> Severity.LOW;
        };
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return message("security_audit_view.title");
    }
}
