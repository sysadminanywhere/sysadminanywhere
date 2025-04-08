package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.model.FunctionalLevel;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Domain")
@Route(value = "domain/info")
@PermitAll
public class DomainView extends VerticalLayout {

    private final LdapService ldapService;

    @SneakyThrows
    public DomainView(LdapService ldapService) {
        this.ldapService = ldapService;

        setPadding(true);
        setMargin(true);

        H3 lblDomain = new H3();
        lblDomain.setText(ldapService.getDomainName().toUpperCase());
        lblDomain.setWidth("100%");

        H5 lblDistinguishedName = new H5();
        lblDistinguishedName.setText(ldapService.getDefaultNamingContext().toUpperCase());
        lblDistinguishedName.setWidth("100%");

        List<Entry> controllers = ldapService.search(new Dn("CN=Sites,CN=Configuration," + ldapService.getDefaultNamingContext()), "(objectClass=server)", SearchScope.SUBTREE);

        Table domainControllers = new Table("Domain controllers");
        for (Entry entry : controllers) {
            String href = "management/computers/" + entry.get("name").getString() + "/details";
            domainControllers.add(entry.get("name").getString(), new Anchor(href, entry.getDn().getName()));
        }

        Entry domainEntry = ldapService.getDomainEntry();

        String ldapTime = domainEntry.get("currentTime").get().getString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SX").withZone(ZoneId.of("UTC"));
        ZonedDateTime dateTime = ZonedDateTime.parse(ldapTime, formatter);

        Table domainProperties = new Table("Properties");
        domainProperties.add("Forest functionality", FunctionalLevel.fromValue(domainEntry.get("forestFunctionality").get().getString()));
        domainProperties.add("Supported SASL mechanisms", ADHelper.getAttributeAsCommaSeparated(domainEntry,"supportedSASLMechanisms"));
        domainProperties.add("Supported LDAP version", ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedLDAPVersion"));
        domainProperties.add("Domain functionality", FunctionalLevel.fromValue(domainEntry.get("domainFunctionality").get().getString()));
        domainProperties.add("Domain controller functionality", FunctionalLevel.fromValue((domainEntry.get("domainControllerFunctionality").get().getString())));
        domainProperties.add("Current time", dateTime.toString());
        domainProperties.add("Max password age", String.valueOf(ldapService.getMaxPwdAgeDays()) + " days");

        add(lblDomain, lblDistinguishedName, domainControllers, domainProperties);
    }

}