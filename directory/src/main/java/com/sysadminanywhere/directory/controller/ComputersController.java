package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.directory.service.ComputersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/computers")
@RequiredArgsConstructor
public class ComputersController {

    private final ComputersService computersService;

    /**
     * Получение всех компьютеров с постраничным выводом и фильтрацией
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ComputerEntry>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort,
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            Pageable pageable = Pageable.ofSize(size).withPage(page);
            Page<ComputerEntry> result = computersService.getAll(pageable, filters, attributes);
            log.info("Retrieved computers with filters");

            PageResponse<ComputerEntry> response = new PageResponse<>(
                    result.getContent(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            );
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving computers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка компьютеров без постраничного вывода
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComputerEntry>> getList(
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            List<ComputerEntry> result = computersService.getAll(filters, attributes);
            log.info("Retrieved computers list with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving computers list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение компьютера по CN (Common Name)
     */
    @GetMapping("/{cn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComputerEntry> getByCN(@PathVariable String cn) {
        try {
            if (cn == null || cn.isBlank()) {
                log.warn("Invalid CN provided");
                return ResponseEntity.badRequest().build();
            }

            ComputerEntry result = computersService.getByCN(cn);
            log.info("Retrieved computer by CN: {}", cn);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving computer by CN: {}", cn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создание нового компьютера
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComputerEntry> add(@Valid @RequestBody AddComputerDto addComputer) {
        try {
            if (addComputer == null || addComputer.getDistinguishedName() == null || addComputer.getDistinguishedName().isBlank()) {
                log.warn("Invalid computer data for creation");
                return ResponseEntity.badRequest().build();
            }

            if (addComputer.getCn() == null || addComputer.getCn().isBlank()) {
                log.warn("CN cannot be empty");
                return ResponseEntity.badRequest().build();
            }

            ComputerEntry result = computersService.add(
                    addComputer.getDistinguishedName(),
                    addComputer.getCn(),
                    addComputer.getDescription(),
                    addComputer.getLocation(),
                    addComputer.isEnabled()
            );

            log.info("Computer created: {}", addComputer.getCn());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            log.error("Error creating computer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновление компьютера
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComputerEntry> update(@Valid @RequestBody ComputerEntry computer) {
        try {
            if (computer == null || computer.getDistinguishedName() == null || computer.getDistinguishedName().isBlank()) {
                log.warn("Invalid computer data for update");
                return ResponseEntity.badRequest().build();
            }

            ComputerEntry result = computersService.update(computer);
            log.info("Computer updated: {}", computer.getDistinguishedName());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error updating computer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удаление компьютера
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @RequestParam @NotBlank(message = "DistinguishedName cannot be empty") String distinguishedName) {

        try {
            computersService.delete(distinguishedName);
            log.info("Computer deleted: {}", distinguishedName);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting computer: {}", distinguishedName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete computer"));
        }
    }

}
