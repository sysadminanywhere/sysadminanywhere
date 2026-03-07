package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import com.sysadminanywhere.directory.service.PrintersService;
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
@RequestMapping("/api/printers")
@RequiredArgsConstructor
public class PrintersController {

    private final PrintersService printersService;

    /**
     * Получение всех принтеров с постраничным выводом и фильтрацией
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PrinterEntry>> getAll(
            @ParameterObject Pageable pageable,
            @RequestParam @NotBlank(message = "Filters cannot be empty") String filters,
            @RequestParam String[] attributes) {

        try {
            validateLdapFilter(filters);
            validateAttributes(attributes);

            Page<PrinterEntry> result = printersService.getAll(pageable, filters, attributes);
            log.info("Retrieved printers with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving printers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка принтеров без постраничного вывода
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrinterEntry>> getList(
            @RequestParam @NotBlank(message = "Filters cannot be empty") String filters,
            @RequestParam String[] attributes) {

        try {
            validateLdapFilter(filters);
            validateAttributes(attributes);

            List<PrinterEntry> result = printersService.getAll(filters, attributes);
            log.info("Retrieved printers list with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving printers list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение принтера по CN (Common Name)
     */
    @GetMapping("/{cn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrinterEntry> getByCN(@PathVariable String cn) {
        try {
            if (cn == null || cn.isBlank()) {
                log.warn("Invalid CN provided");
                return ResponseEntity.badRequest().build();
            }

            PrinterEntry result = printersService.getByCN(cn);
            log.info("Retrieved printer by CN: {}", cn);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving printer by CN: {}", cn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удаление принтера
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @RequestParam @NotBlank(message = "DistinguishedName cannot be empty") String distinguishedName) {

        try {
            printersService.delete(distinguishedName);
            log.info("Printer deleted: {}", distinguishedName);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting printer: {}", distinguishedName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete printer"));
        }
    }

    /**
     * Валидация LDAP фильтра на предмет LDAP injection
     */
    private void validateLdapFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            throw new IllegalArgumentException("LDAP filter cannot be empty");
        }

        if (filter.contains("*") && filter.length() > 100) {
            throw new IllegalArgumentException("Invalid LDAP filter format");
        }
    }

    /**
     * Валидация атрибутов
     */
    private void validateAttributes(String[] attributes) {
        if (attributes == null || attributes.length == 0) {
            throw new IllegalArgumentException("Attributes cannot be empty");
        }

        for (String attr : attributes) {
            if (attr == null || attr.isBlank()) {
                throw new IllegalArgumentException("Attribute cannot be empty");
            }
        }
    }
}