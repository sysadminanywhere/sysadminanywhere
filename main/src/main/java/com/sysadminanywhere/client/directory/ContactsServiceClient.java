package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface ContactsServiceClient {

    @GetExchange("/api/contacts")
    Page<ContactEntry> getAll(Pageable pageable, String filters, String[] attributes);

    @GetExchange("/api/contacts/list")
    List<ContactEntry> getList(String filters, String[] attributes);

    @GetExchange("/api/contacts/{cn}")
    ContactEntry getByCN(String cn);

    @PostExchange("/api/contacts")
    ContactEntry add(AddContactDto addContact);

    @PutExchange("/api/contacts")
    ContactEntry update(ContactEntry contact);

    @DeleteExchange("/api/contacts")
    void delete(String distinguishedName);

}
