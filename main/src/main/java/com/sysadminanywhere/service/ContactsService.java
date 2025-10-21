package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.ContactsServiceClient;
import com.sysadminanywhere.common.directory.dto.AddContactDto;
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
        return contactsServiceClient.add(new AddContactDto(distinguishedName, contact));
    }

    public ContactEntry update(ContactEntry contact) {
        return contactsServiceClient.update(contact);
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        contactsServiceClient.delete(distinguishedName);
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}