package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.domain.ADHelper;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.FunctionalLevel;
import com.sysadminanywhere.service.LdapService;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.SneakyThrows;

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
        lblDistinguishedName.getStyle().setMarginBottom("20px");

        add(lblDomain, lblDistinguishedName, getControllers(), getProperties());
    }

    @SneakyThrows
    private Card getControllers(){
        Card card = new Card();
        card.setTitle("Domain controllers");

        List<EntryDto> controllers = ldapService.search("CN=Sites,CN=Configuration," + ldapService.getDefaultNamingContext(), "(objectClass=server)", SearchScope.SUBTREE);

        Table domainControllers = new Table("");
        for (EntryDto entry : controllers) {
            String href = "management/computers/" + entry.getAttributes().get("name").toString() + "/details";
            domainControllers.add(entry.getAttributes().get("name").toString(), new Anchor(href, entry.getDn()));
        }

        card.add(domainControllers);

        return card;
    }

    private Card getProperties(){
        Card card = new Card();
        card.setTitle("Properties");

        EntryDto domainEntry = ldapService.getRootDse();

        String ldapTime = domainEntry.getAttributes().get("currenttime").toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SX").withZone(ZoneId.of("UTC"));
        ZonedDateTime dateTime = ZonedDateTime.parse(ldapTime, formatter);

        Table domainProperties = new Table("");
        domainProperties.add("Forest functionality", FunctionalLevel.fromValue(domainEntry.getAttributes().get("forestfunctionality").toString()));
        domainProperties.add("Supported SASL mechanisms", ADHelper.getAttributeAsCommaSeparated(domainEntry,"supportedsaslmechanisms"));
        domainProperties.add("Supported LDAP version", ADHelper.getAttributeAsCommaSeparated(domainEntry, "supportedldapversion"));
        domainProperties.add("Domain functionality", FunctionalLevel.fromValue(domainEntry.getAttributes().get("domainfunctionality").toString()));
        domainProperties.add("Domain controller functionality", FunctionalLevel.fromValue((domainEntry.getAttributes().get("domaincontrollerfunctionality").toString())));
        domainProperties.add("Current time", dateTime.toString());
        domainProperties.add("Max password age", String.valueOf(ldapService.getMaxPwdAgeDays()) + " days");

        card.add(domainProperties);

        return card;
    }

}