package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.ContactEntry;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactsService {

    private final LdapService ldapService;

    ResolveService<ContactEntry> resolveService = new ResolveService<>(ContactEntry.class);

    public ContactsService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @SneakyThrows
    public Page<ContactEntry> getAll(Pageable pageable, String filters, String... attributes) {
        Page<Entry> result = ldapService.searchPage("(&(objectClass=contact)(objectCategory=person)" + filters + ")", pageable.getSort(), pageable, attributes);
        return resolveService.getADPage(result);
    }

    @SneakyThrows
    public List<ContactEntry> getAll(String filters, String... attributes) {
        List<Entry> result = ldapService.searchWithAttributes("(&(objectClass=contact)(objectCategory=person)" + filters + ")", attributes);
        return resolveService.getADList(result);
    }

    public ContactEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=contact)(objectCategory=person)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        if (entry.isPresent())
            return resolveService.getADValue(entry.get());
        else
            return null;
    }

    @SneakyThrows
    public ContactEntry add(String distinguishedName, String cn, String displayName, String firstName, String lastName, String initials) {
        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + cn + "," + ldapService.getUsersContainer();
        } else {
            dn = "cn=" + cn + "," + distinguishedName;
        }

        Entry entry = new DefaultEntry(
                dn,
                "displayName", displayName,
                "givenName", firstName,
                "sn", lastName,
                "objectClass:contact",
                "objectClass:person",
                "cn", cn
        );

        ldapService.add(entry);

        ContactEntry newContact = getByCN(cn);

        if (initials != null && !initials.isEmpty())
            ldapService.updateProperty(newContact.getDistinguishedName(), "initials", initials);

        return newContact;
    }

    public ContactEntry update(ContactEntry contact) {
        ModifyRequest modifyRequest = resolveService.getModifyRequest(contact, getByCN(contact.getCn()));
        ldapService.update(modifyRequest);

        return getByCN(contact.getCn());
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}