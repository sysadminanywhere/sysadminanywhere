package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.DomainHealthDto;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
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

import java.util.List;

@Route(value = "domain/health")
@RolesAllowed({"ADMIN", "READER"})
public class DomainHealthView extends VerticalLayout implements HasDynamicTitle {
    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;
    private final H3 overall = new H3();
    private final Span checkedAt = new Span();
    private final Grid<DomainHealthDto.DomainHealthCheckDto> grid = new Grid<>();

    public DomainHealthView(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button refresh = new Button(message("common.refresh"), event -> refresh());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout header = new HorizontalLayout(overall, checkedAt, refresh);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, overall);

        grid.addColumn(DomainHealthDto.DomainHealthCheckDto::getName)
                .setHeader(message("domain_health_view.check")).setAutoWidth(true);
        grid.addComponentColumn(item -> status(item.getStatus()))
                .setHeader(message("domain_health_view.status")).setAutoWidth(true);
        grid.addColumn(DomainHealthDto.DomainHealthCheckDto::getDetails)
                .setHeader(message("domain_health_view.details")).setFlexGrow(1);
        grid.setWidthFull();
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        add(header, grid);
        refresh();
    }

    private void refresh() {
        DomainHealthDto result = ldapService.getDomainHealth();
        if (result == null) {
            result = new DomainHealthDto("ERROR", null, List.of());
        }
        overall.setText(message("domain_health_view.overall") + ": " + localizedStatus(result.getOverallStatus()));
        checkedAt.setText(result.getCheckedAt() == null ? "" :
                message("domain_health_view.checked_at") + ": " + result.getCheckedAt());
        grid.setItems(result.getChecks() == null ? List.of() : result.getChecks());
        if ("ERROR".equals(result.getOverallStatus())) {
            Notification notification = Notification.show(message("domain_health_view.error"));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Span status(String value) {
        Span badge = new Span(localizedStatus(value));
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add("ERROR".equals(value) ? "error" :
                "WARNING".equals(value) ? "warning" : "HEALTHY".equals(value) ? "success" : "contrast");
        return badge;
    }

    private String localizedStatus(String value) {
        return switch (value == null ? "UNKNOWN" : value) {
            case "HEALTHY" -> message("domain_health_view.healthy");
            case "WARNING" -> message("domain_health_view.warning");
            case "ERROR" -> message("domain_health_view.error");
            default -> message("domain_health_view.not_checked");
        };
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @Override
    public String getPageTitle() {
        return message("domain_health_view.title");
    }
}
