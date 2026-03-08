package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddContactDto;
import com.sysadminanywhere.common.directory.model.ContactEntry;
import com.sysadminanywhere.directory.service.ContactsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactsController {

    private final ContactsService contactsService;

    /**
     * Получение всех контактов с постраничным выводом и фильтрацией
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactEntry>> getAll(
            @ParameterObject Pageable pageable,
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            Page<ContactEntry> result = contactsService.getAll(pageable, filters, attributes);
            log.info("Retrieved contacts with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка контактов без постраничного вывода
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactEntry>> getList(
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            List<ContactEntry> result = contactsService.getAll(filters, attributes);
            log.info("Retrieved contacts list with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving contacts list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение контакта по CN (Common Name)
     */
    @GetMapping("/{cn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactEntry> getByCN(@PathVariable String cn) {
        try {
            if (cn == null || cn.isBlank()) {
                log.warn("Invalid CN provided");
                return ResponseEntity.badRequest().build();
            }

            ContactEntry result = contactsService.getByCN(cn);
            log.info("Retrieved contact by CN: {}", cn);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving contact by CN: {}", cn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создание нового контакта
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactEntry> add(@Valid @RequestBody AddContactDto addContact) {
        try {
            if (addContact == null || addContact.getDistinguishedName() == null || addContact.getDistinguishedName().isBlank()) {
                log.warn("Invalid contact data for creation");
                return ResponseEntity.badRequest().build();
            }

            if (addContact.getCn() == null || addContact.getCn().isBlank()) {
                log.warn("CN cannot be empty");
                return ResponseEntity.badRequest().build();
            }

            ContactEntry result = contactsService.add(
                    addContact.getDistinguishedName(),
                    addContact.getCn(),
                    addContact.getDisplayName(),
                    addContact.getFirstName(),
                    addContact.getLastName(),
                    addContact.getInitials()
            );

            log.info("Contact created: {}", addContact.getCn());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("Error creating contact", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновление контакта
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactEntry> update(@Valid @RequestBody ContactEntry contact) {
        try {
            if (contact == null || contact.getDistinguishedName() == null || contact.getDistinguishedName().isBlank()) {
                log.warn("Invalid contact data for update");
                return ResponseEntity.badRequest().build();
            }

            ContactEntry result = contactsService.update(contact);
            log.info("Contact updated: {}", contact.getDistinguishedName());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error updating contact", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удаление контакта
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @RequestParam @NotBlank(message = "DistinguishedName cannot be empty") String distinguishedName) {

        try {
            contactsService.delete(distinguishedName);
            log.info("Contact deleted: {}", distinguishedName);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting contact: {}", distinguishedName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete contact"));
        }
    }

}