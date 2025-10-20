package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.ContactsServiceClient;
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
    private final ContactsServiceClient contactsServiceClient;

    ResolveService<ContactEntry> resolveService = new ResolveService<>(ContactEntry.class);

    public ContactsService(LdapService ldapService, ContactsServiceClient contactsServiceClient) {
        this.ldapService = ldapService;
        this.contactsServiceClient = contactsServiceClient;
    }

    @SneakyThrows
    public Page<ContactEntry> getAll(Pageable pageable, String filters) {
        return contactsServiceClient.getAll(pageable, filters);
    }

    @SneakyThrows
    public List<ContactEntry> getAll(String filters) {
        return contactsServiceClient.getList(filters);
    }

    public ContactEntry getByCN(String cn) {
        return contactsServiceClient.getList("(&(objectClass=contact)(objectCategory=person)(cn=" + cn + "))").getFirst();
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