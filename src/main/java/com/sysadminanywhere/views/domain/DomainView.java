package com.sysadminanywhere.views.domain;

import com.sysadminanywhere.control.Table;
import com.sysadminanywhere.service.LdapService;
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

import java.util.List;
import java.util.Map;

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
            domainControllers.add(entry.get("name").getString(), entry.getDn().getName());
        }

        add(lblDomain, lblDistinguishedName, domainControllers);
    }

}