package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.directory.service.ContactsService;
import lombok.NonNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final ContactsService contactsService;

    public ContactsController(ContactsService contactsService) {
        this.contactsService = contactsService;
    }

    @GetMapping()
    public ResponseEntity<Page<ContactEntry>> getAll(@ParameterObject Pageable pageable, @RequestParam String filters, @RequestParam String[] attributes) {
        return new ResponseEntity<>(contactsService.getAll(pageable, filters, attributes), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ContactEntry>> getList(@RequestParam String filters) {
        return new ResponseEntity<>(contactsService.getAll(filters), HttpStatus.OK);
    }

    @GetMapping("/{cn}")
    public ResponseEntity<ContactEntry> getByCN(@PathVariable String cn) {
        return new ResponseEntity<>(contactsService.getByCN(cn), HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ContactEntry> add(@NonNull @RequestBody AddContactDto addContact){
        return new ResponseEntity<>(contactsService.add(addContact.getDistinguishedName(), addContact.getContact()), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<ContactEntry> update(@NonNull @RequestBody ContactEntry contact) {
        return new ResponseEntity<>(contactsService.update(contact), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@NonNull @RequestParam String distinguishedName) {
        contactsService.delete(distinguishedName);
        return new ResponseEntity(HttpStatus.OK);
    }

}