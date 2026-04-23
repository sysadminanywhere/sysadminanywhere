package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface ContactsServiceClient {

    @GetExchange("/api/contacts")
    PageResponse<ContactEntry> getAll(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/contacts/list")
    List<ContactEntry> getList(@RequestParam String filters, @RequestParam String[] attributes);

    @GetExchange("/api/contacts/{cn}")
    ContactEntry getByCN(@PathVariable String cn);

    @PostExchange("/api/contacts")
    ContactEntry add(@RequestBody AddContactDto addContact);

    @PutExchange("/api/contacts")
    ContactEntry update(@RequestBody ContactEntry contact);

    @DeleteExchange("/api/contacts")
    void delete(@RequestParam String distinguishedName);

}
