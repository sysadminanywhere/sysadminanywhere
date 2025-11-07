package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.ContactsServiceClient;
import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContactsService {

    private final LdapService ldapService;
    private final ContactsServiceClient contactsServiceClient;

    public ContactsService(LdapService ldapService, ContactsServiceClient contactsServiceClient) {
        this.ldapService = ldapService;
        this.contactsServiceClient = contactsServiceClient;
    }

    @SneakyThrows
    public Page<ContactEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            return contactsServiceClient.getAll(pageable, filters, attributes);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @SneakyThrows
    public List<ContactEntry> getAll(String filters) {
        return contactsServiceClient.getList(filters);
    }

    public List<ContactEntry> getAll() {
        List<EntryDto> list = ldapService.searchWithAttributes("(&(objectClass=contact)(objectCategory=person))",
                "cn");

        List<ContactEntry> items = new ArrayList<>();

        if(list != null) {
            for (EntryDto entryDto : list) {
                ContactEntry item = new ContactEntry();
                item.setCn(entryDto.getAttributes().get("cn").toString());
                items.add(item);
            }
        }

        return items;
    }

    public ContactEntry getByCN(String cn) {
        return contactsServiceClient.getByCN(cn);
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