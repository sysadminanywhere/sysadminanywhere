package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "contacts",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface ContactsServiceClient {

    @GetMapping("/api/contacts")
    Page<ContactEntry> getAll(Pageable pageable, @RequestParam("filters") String filters, @RequestParam("attributes") String[] attributes);

    @GetMapping("/api/contacts/list")
    List<ContactEntry> getList(@RequestParam("filters") String filters);

    @GetMapping("/api/contacts/{cn}")
    ContactEntry getByCN(@PathVariable("cn") String cn);

    @PostMapping("/api/contacts")
    ContactEntry add(@RequestBody AddContactDto addContact);

    @PutMapping("/api/contacts")
    ContactEntry update(@RequestBody ContactEntry contact);

    @DeleteMapping("/api/contacts")
    void delete(@RequestParam("distinguishedName") String distinguishedName);

}