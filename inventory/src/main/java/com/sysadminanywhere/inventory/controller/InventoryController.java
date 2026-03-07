package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;

    /**
     * Получение ПО на компьютере с постраничным выводом
     */
    @GetMapping("/computers/{computerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SoftwareOnComputer>> getSoftwareOnComputer(
            @PathVariable Long computerId,
            Pageable pageable) {

        if (computerId == null || computerId <= 0) {
            log.warn("Invalid computerId: {}", computerId);
            return ResponseEntity.badRequest().build();
        }

        Page<SoftwareOnComputer> result = softwareRepository.getSoftwareOnComputer(computerId, pageable);
        log.info("Retrieved software for computer: {}", computerId);

        return ResponseEntity.ok(result);
    }

    /**
     * Получение статистики установок ПО с фильтрацией по названию и вендору
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SoftwareCount>> getSoftwareCount(
            @RequestParam @NotBlank(message = "Name cannot be empty") String name,
            @RequestParam @NotBlank(message = "Vendor cannot be empty") String vendor,
            Pageable pageable) {

        // Параметры уже валидны благодаря @NotBlank
        // Используйте % в SQL запросе, а не здесь
        String namePattern = "%" + sanitizeSearchTerm(name) + "%";
        String vendorPattern = "%" + sanitizeSearchTerm(vendor) + "%";

        Page<SoftwareCount> result = softwareRepository.getSoftwareInstallationCount(
                namePattern, vendorPattern, pageable);

        log.info("Retrieved software count for name: {}, vendor: {}", name, vendor);

        return ResponseEntity.ok(result);
    }

    /**
     * Получение компьютеров с установленным ПО
     */
    @GetMapping("/{softwareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ComputerItem>> getComputersWithSoftware(
            @PathVariable Long softwareId,
            @RequestParam @NotBlank(message = "Name cannot be empty") String name,
            Pageable pageable) {

        if (softwareId == null || softwareId <= 0) {
            log.warn("Invalid softwareId: {}", softwareId);
            return ResponseEntity.badRequest().build();
        }

        String namePattern = "%" + sanitizeSearchTerm(name) + "%";
        Page<ComputerItem> result = computerRepository.getComputersWithSoftware(
                softwareId, namePattern, pageable);

        log.info("Retrieved computers with software: {}", softwareId);

        return ResponseEntity.ok(result);
    }

    /**
     * Санитизация поисковых терминов для предотвращения SQL injection
     */
    private String sanitizeSearchTerm(String term) {
        if (term == null) {
            return "";
        }
        // Экранирование специальных символов SQL LIKE
        return term.replace("'", "''")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

}