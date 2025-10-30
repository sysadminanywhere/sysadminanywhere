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
    public Page<ContactEntry> getAll(Pageable pageable, String filters) {
        Page<Entry> result = ldapService.searchPage("(&(objectClass=contact)(objectCategory=person)" + filters + ")", pageable.getSort(), pageable);
        return resolveService.getADPage(result);
    }

    @SneakyThrows
    public List<ContactEntry> getAll(String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=contact)(objectCategory=person)" + filters + ")");
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
    public ContactEntry add(String distinguishedName, ContactEntry contact) {
        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + contact.getCn() + "," + ldapService.getUsersContainer();
        } else {
            dn = "cn=" + contact.getCn() + "," + distinguishedName;
        }

        Entry entry = new DefaultEntry(
                dn,
                "displayName", contact.getDisplayName(),
                "givenName", contact.getFirstName(),
                "sn", contact.getLastName(),
                "objectClass:contact",
                "objectClass:person",
                "cn", contact.getCn()
        );

        ldapService.add(entry);

        ContactEntry newContact = getByCN(contact.getCn());

        if (contact.getInitials() != null && !contact.getInitials().isEmpty())
            ldapService.updateProperty(newContact.getDistinguishedName(), "initials", contact.getInitials());

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