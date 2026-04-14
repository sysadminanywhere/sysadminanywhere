package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.FunctionalLevel;
import com.sysadminanywhere.service.LocaleService;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.context.MessageSource;
import lombok.SneakyThrows;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RolesAllowed("ADMIN")
@Route(value = "domain/info")
public class DomainView extends VerticalLayout implements HasDynamicTitle {

    private final LdapService ldapService;
    private final MessageSource messageSource;
    private final LocaleService localeService;

    @SneakyThrows
    public DomainView(LdapService ldapService, MessageSource messageSource, LocaleService localeService) {
        this.ldapService = ldapService;
        this.messageSource = messageSource;
        this.localeService = localeService;

        setPadding(true);
        setMargin(true);

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");
        lblDistinguishedName.getStyle().setMarginBottom("20px");

        add(lblDomain, lblDistinguishedName, getControllers(), getProperties());
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, localeService.getCurrentLocale());
    }

    @SneakyThrows
    private Card getControllers(){
        Card card = new Card();
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
        card.setTitle(getMessage("common.details"));

        EntryDto domainEntry = ldapService.getRootDse();

        if(domainEntry != null) {
            String ldapTime = domainEntry.getAttributes().get("currenttime").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SX").withZone(ZoneId.of("UTC"));
            ZonedDateTime dateTime = ZonedDateTime.parse(ldapTime, formatter);

            Table domainProperties = new Table("");
            domainProperties.add("Forest functionality", FunctionalLevel.fromValue(domainEntry.getAttributes().get("forestfunctionality").toString()));
            domainProperties.add("Supported SASL mechanisms", ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedsaslmechanisms"));
            domainProperties.add("Supported LDAP version", ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedldapversion"));
            domainProperties.add("Domain functionality", FunctionalLevel.fromValue(domainEntry.getAttributes().get("domainfunctionality").toString()));
            domainProperties.add("Domain controller functionality", FunctionalLevel.fromValue((domainEntry.getAttributes().get("domaincontrollerfunctionality").toString())));
            domainProperties.add("Current time", dateTime.toString());
            domainProperties.add("Max password age", String.valueOf(ldapService.getMaxPwdAgeDays()) + " days");

            card.add(domainProperties);
        }

        return card;
    }

    public String getPageTitle() {
        return getMessage("domain_view.title");
    }

}
