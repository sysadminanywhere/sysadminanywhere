package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.DomainHealthDto;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.FunctionalLevel;
import com.sysadminanywhere.service.LdapService;
import com.sysadminanywhere.service.LocaleService;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import lombok.SneakyThrows;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

@RolesAllowed({"ADMIN", "READER"})
@Route(value = "domain/info")
public class DomainView extends VerticalLayout implements HasDynamicTitle {

    private static final DateTimeFormatter HEALTH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    @SneakyThrows
    public DomainView(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        addClassNames("gridwith-filters-view");
        setSizeFull();

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");
        lblDistinguishedName.getStyle().setMarginBottom("20px");

        add(lblDomain, lblDistinguishedName, getControllers(), getProperties(), getHealth());
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @SneakyThrows
    private Card getControllers(){
        Card card = new Card();
        card.setWidthFull();
        card.setMinWidth("0");
        card.setTitle(getMessage("dashboard_view.domain_controllers"));

        List<EntryDto> controllers = ldapService.search("CN=Sites,CN=Configuration," + ldapService.getDefaultNamingContext(), "(objectClass=server)", SearchScope.SUBTREE);

        if(controllers != null) {
            Table domainControllers = new Table("");
            for (EntryDto entry : controllers) {
                String href = "management/computers/" + entry.getAttributes().get("name").toString() + "/details";
                domainControllers.add(entry.getAttributes().get("name").toString(), new Anchor(href, entry.getDn()));
            }

            card.add(domainControllers);
        }

        return card;
    }

    private Card getProperties(){
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(getMessage("common.details"));

        EntryDto domainEntry = ldapService.getRootDse();

        if(domainEntry != null) {
            String ldapTime = domainEntry.getAttributes().get("currenttime").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SX").withZone(ZoneId.of("UTC"));
            ZonedDateTime dateTime = ZonedDateTime.parse(ldapTime, formatter);

            Table domainProperties = new Table("");
            domainProperties.add(getMessage("domain_view.forest_functionality"), FunctionalLevel.fromValue(domainEntry.getAttributes().get("forestfunctionality").toString()));
            domainProperties.add(getMessage("domain_view.supported_sasl_mechanisms"), ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedsaslmechanisms"));
            domainProperties.add(getMessage("domain_view.supported_ldap_version"), ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedldapversion"));
            domainProperties.add(getMessage("domain_view.domain_functionality"), FunctionalLevel.fromValue(domainEntry.getAttributes().get("domainfunctionality").toString()));
            domainProperties.add(getMessage("domain_view.domain_controller_functionality"), FunctionalLevel.fromValue((domainEntry.getAttributes().get("domaincontrollerfunctionality").toString())));
            domainProperties.add(getMessage("domain_view.current_time"), dateTime.toString());
            domainProperties.add(getMessage("domain_view.max_password_age"), ldapService.getMaxPwdAgeDays() + " days");

            card.add(domainProperties);
        }

        return card;
    }

    private Card getHealth() {
        Card card = new Card();
        card.setWidthFull();
        card.setMinWidth("0");
        card.setTitle(getMessage("domain_health_view.title"));
        H5 overall = new H5();
        Span checkedAt = new Span();
        Button refresh = new Button(getMessage("common.refresh"));
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Grid<DomainHealthDto.DomainHealthCheckDto> grid = new Grid<>();
        grid.addColumn(DomainHealthDto.DomainHealthCheckDto::getName)
                .setHeader(getMessage("domain_health_view.check")).setAutoWidth(true);
        grid.addComponentColumn(item -> healthStatus(item.getStatus()))
                .setHeader(getMessage("domain_health_view.status")).setAutoWidth(true);
        grid.addColumn(DomainHealthDto.DomainHealthCheckDto::getDetails)
                .setHeader(getMessage("domain_health_view.details")).setFlexGrow(1);
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        Runnable load = () -> {
            DomainHealthDto result = ldapService.getDomainHealth();
            if (result == null) result = new DomainHealthDto("ERROR", null, List.of());
            overall.setText(getMessage("domain_health_view.overall") + ": " + localizedHealthStatus(result.getOverallStatus()));
            checkedAt.setText(result.getCheckedAt() == null ? "" :
                    getMessage("domain_health_view.checked_at") + ": " + formatHealthTime(result.getCheckedAt()));
            grid.setItems(result.getChecks() == null ? List.of() : result.getChecks());
            if ("ERROR".equals(result.getOverallStatus())) {
                Notification notification = Notification.show(getMessage("domain_health_view.error"));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };
        refresh.addClickListener(event -> load.run());
        HorizontalLayout header = new HorizontalLayout(overall, checkedAt, refresh);
        header.addClassName("domain-health-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, overall);
        card.add(header, grid);
        load.run();
        return card;
    }

    private String formatHealthTime(LocalDateTime value) {
        return value == null ? "" : value.format(HEALTH_TIME_FORMAT);
    }

    private Span healthStatus(String value) {
        Span badge = new Span(localizedHealthStatus(value));
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add("ERROR".equals(value) ? "error" :
                "WARNING".equals(value) ? "warning" : "HEALTHY".equals(value) ? "success" : "contrast");
        return badge;
    }

    private String localizedHealthStatus(String value) {
        return switch (value == null ? "UNKNOWN" : value) {
            case "HEALTHY" -> getMessage("domain_health_view.healthy");
            case "WARNING" -> getMessage("domain_health_view.warning");
            case "ERROR" -> getMessage("domain_health_view.error");
            default -> getMessage("domain_health_view.not_checked");
        };
    }

    public String getPageTitle() {
        return getMessage("domain_view.title");
    }

}
