package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.inventory.model.*;
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.HardwareModel;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.HardwareModelRepository;
import com.sysadminanywhere.inventory.repository.HardwarePropertyRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.repository.SoftwareLicenseRepository;
import com.sysadminanywhere.inventory.entity.SoftwareLicense;
import com.sysadminanywhere.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final ComputerHardwareRepository computerHardwareRepository;
    private final HardwareModelRepository hardwareModelRepository;
    private final HardwarePropertyRepository hardwarePropertyRepository;
    private final SoftwareLicenseRepository softwareLicenseRepository;
    @org.springframework.beans.factory.annotation.Value("${inventory.vulnerability.rules:}")
    private String vulnerabilityRules;

    @org.springframework.beans.factory.annotation.Value("${inventory.patch.stale-days:90}")
    private int patchStaleDays;

    @GetMapping("/licenses")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<List<com.sysadminanywhere.common.inventory.model.SoftwareLicense>> getLicenses() {
        return ResponseEntity.ok(softwareLicenseRepository.findAll().stream().map(this::licenseDto).toList());
    }

    @PostMapping("/licenses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.sysadminanywhere.common.inventory.model.SoftwareLicense> saveLicense(
            @RequestBody com.sysadminanywhere.common.inventory.model.SoftwareLicense request) {
        SoftwareLicense entity = new SoftwareLicense();
        entity.setId(request.id()); entity.setName(request.name()); entity.setVendor(request.vendor());
        entity.setVersion(request.version()); entity.setPurchased(request.purchased());
        entity.setExpiresAt(request.expiresAt()); entity.setNotes(request.notes());
        return ResponseEntity.ok(licenseDto(softwareLicenseRepository.save(entity)));
    }

    @DeleteMapping("/licenses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLicense(@PathVariable Long id) {
        softwareLicenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private com.sysadminanywhere.common.inventory.model.SoftwareLicense licenseDto(SoftwareLicense item) {
        long used = softwareRepository.countInstallations(item.getName(), item.getVendor(), item.getVersion());
        return new com.sysadminanywhere.common.inventory.model.SoftwareLicense(item.getId(), item.getName(), item.getVendor(),
                item.getVersion(), item.getPurchased(), used, item.getExpiresAt(), item.getNotes());
    }
    private final InventoryService inventoryService;
    private final com.sysadminanywhere.inventory.service.InventoryScheduler inventoryScheduler;

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<InventorySchedule> getSchedule() {
        return ResponseEntity.ok(inventoryScheduler.getSchedule());
    }

    @PutMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventorySchedule> updateSchedule(@RequestBody InventorySchedule schedule) {
        try {
            return ResponseEntity.ok(inventoryScheduler.update(schedule.cron(), schedule.enabled()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> startScan(@RequestBody(required = false) InventoryScanRequest request) {
        List<String> names = request == null ? List.of() : request.computerNames();
        return inventoryService.startScan(names) ? ResponseEntity.accepted().build() : ResponseEntity.status(409).build();
    }

    @GetMapping("/scan/status")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<InventoryScanStatus> getScanStatus() {
        return ResponseEntity.ok(inventoryService.getScanStatus());
    }

    @PostMapping("/scan/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelScan() {
        return inventoryService.cancelScan() ? ResponseEntity.accepted().build() : ResponseEntity.noContent().build();
    }

    @GetMapping("/scan/history")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<List<InventoryScanRun>> getScanHistory() {
        return ResponseEntity.ok(inventoryService.getScanHistory());
    }

    // Software

    @GetMapping("/software/vulnerabilities")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<List<SoftwareVulnerability>> getSoftwareVulnerabilities() {
        if (vulnerabilityRules == null || vulnerabilityRules.isBlank()) return ResponseEntity.ok(List.of());
        List<SoftwareVulnerability> result = new java.util.ArrayList<>();
        for (String rule : vulnerabilityRules.split("\\s*;\\s*")) {
            String[] parts = rule.split("\\|", -1);
            if (parts.length < 4) continue;
            String name = parts[0].trim();
            String version = parts[1].trim();
            for (var software : softwareRepository.findAll()) {
                boolean nameMatches = software.getName() != null && software.getName().equalsIgnoreCase(name);
                boolean versionMatches = "*".equals(version) || (software.getVersion() != null && software.getVersion().equalsIgnoreCase(version));
                if (nameMatches && versionMatches) {
                    result.add(new SoftwareVulnerability(software.getName(), software.getVendor(), software.getVersion(),
                            parts[2].trim().toUpperCase(Locale.ROOT), parts[3].trim(),
                            softwareRepository.countInstallations(software.getName(), software.getVendor(), software.getVersion())));
                }
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<InventoryHealthDto> getInventoryHealth(
            @RequestParam(defaultValue = "30") int staleDays) {
        if (staleDays < 1 || staleDays > 3650) {
            return ResponseEntity.badRequest().build();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusDays(staleDays);
        List<com.sysadminanywhere.inventory.entity.Computer> tracked = computerRepository.findAll();
        List<InventoryHealthComputer> stale = tracked.stream()
                .filter(computer -> computer.getCheckingDate() == null || computer.getCheckingDate().isBefore(threshold))
                .map(computer -> new InventoryHealthComputer(
                        computer.getId(), computer.getName(), computer.getCheckingDate(),
                        computer.getCheckingDate() == null ? -1 : Duration.between(computer.getCheckingDate(), now).toDays(),
                        computer.getLastScanStatus(), computer.getLastScanError()))
                .sorted(Comparator.comparing(InventoryHealthComputer::checkingDate,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
        int neverScanned = (int) tracked.stream().filter(computer -> computer.getCheckingDate() == null).count();
        InventoryHealthDto response = new InventoryHealthDto(now, staleDays, tracked.size(),
                stale.size(), neverScanned, stale);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/software/count")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<PageResponse<SoftwareCount>> getSoftwareCount(
            @RequestParam String name,
            @RequestParam String vendor,
            @RequestParam(required = false) Long minCount,
            @RequestParam(required = false) Long maxCount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort) {

        String requestedName = name;
        String requestedVendor = vendor;
        name = name + "%";
        vendor = vendor + "%";
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<SoftwareCount> result = softwareRepository.getSoftwareInstallationCount(
                name, vendor, minCount, maxCount, pageable);

        log.info("Retrieved software count for name: {}, vendor: {}",
                displayFilter(requestedName), displayFilter(requestedVendor));

        PageResponse<SoftwareCount> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    private String displayFilter(String value) {
        return value == null || value.isBlank() ? "<all>" : value;
    }

    @GetMapping("/computers/{computerId}/software")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<PageResponse<SoftwareOnComputer>> getSoftwareOnComputer(
            @PathVariable Long computerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort) {

        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<SoftwareOnComputer> result = softwareRepository.getSoftwareOnComputer(computerId, pageable);
        log.info("Retrieved software for computer: {}", computerId);

        PageResponse<SoftwareOnComputer> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/software/{softwareId}")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<PageResponse<ComputerItem>> getComputersWithSoftware(
            @PathVariable Long softwareId,
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort) {

        name = name + "%";
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<ComputerItem> result = computerRepository.getComputersWithSoftware(
                softwareId, name, pageable);

        log.info("Retrieved computers with software: {}", softwareId);

        PageResponse<ComputerItem> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }


    // Hardware

    @GetMapping("/hardware/computers")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<PageResponse<com.sysadminanywhere.common.inventory.model.HardwareComputerItem>> getHardwareComputers(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = inventoryService.getComputerHardwareSummaries(name,
                org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size))));
        return ResponseEntity.ok(new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages()));
    }

    @GetMapping("/hardware/computers/{computerId}")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<com.sysadminanywhere.common.inventory.model.ComputerHardwareDetails> getComputerHardwareDetails(
            @PathVariable Long computerId) {
        var details = inventoryService.getComputerHardwareDetails(computerId);
        return details == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(details);
    }

    @GetMapping("/hardware/operating-systems")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<List<OperatingSystemCount>> getOperatingSystemCounts() {
        return ResponseEntity.ok(hardwareModelRepository.findOperatingSystemCounts().stream()
                .map(row -> new OperatingSystemCount((String) row[0], ((Number) row[1]).longValue()))
                .toList());
    }

    @GetMapping("/hardware/coverage")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<InventoryCoverage> getInventoryCoverage() {
        long computers = computerRepository.count();
        long operatingSystems = hardwareModelRepository.countComputersWithHardwareType("OperatingSystem");
        long patches = hardwareModelRepository.countComputersWithHardwareType("Patch");
        return ResponseEntity.ok(new InventoryCoverage(computers, operatingSystems, patches,
                softwareRepository.countWithoutVersion(), Math.max(1, patchStaleDays)));
    }

    @GetMapping("/hardware/patches/status")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<List<ComputerPatchStatus>> getPatchStatuses() {
        LocalDate threshold = LocalDate.now().minusDays(Math.max(1, patchStaleDays));
        return ResponseEntity.ok(computerHardwareRepository.findPatchStatuses().stream()
                .map(item -> new ComputerPatchStatus(item.computer(), item.lastPatchDate(), isPatchStale(item.lastPatchDate(), threshold)))
                .toList());
    }

    private boolean isPatchStale(String value, LocalDate threshold) {
        if (value == null || value.isBlank()) return true;
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("MM/dd/yyyy"), DateTimeFormatter.ofPattern("M/d/yyyy"))) {
            try { return LocalDate.parse(value.trim(), formatter).isBefore(threshold); }
            catch (DateTimeParseException ignored) { }
        }
        return true;
    }

    @GetMapping("/hardware")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<PageResponse<HardwareItem>> getHardwares(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sort) {

        name = name + "%";
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<HardwareItem> result = hardwareModelRepository.findByNameAndType(name, type, pageable);

        log.info("Retrieved hardwares: {}", result.getTotalElements());

        PageResponse<HardwareItem> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hardware/{hardwareId}")
    @PreAuthorize("hasRole('ADMIN') or @apiTokenAuthorization.isAllowed()")
    public ResponseEntity<HardwareModelItem> getHardwareProperties(
            @PathVariable Long hardwareId) {

        HardwareModel hardwareModel = hardwareModelRepository.findById(hardwareId)
                .orElseThrow(() -> new RuntimeException("Hardware model not found with id: " + hardwareId));

        List<ComputerHardware> computerHardwares = computerHardwareRepository.findAll().stream()
                .filter(ch -> ch.getHardwareModel().getId().equals(hardwareId))
                .toList();
        
        List<HardwarePropertyItem> properties = computerHardwares.stream()
                .flatMap(ch -> ch.getProperties().stream())
                .map(prop -> new HardwarePropertyItem(
                        prop.getId(),
                        prop.getPropertyName(),
                        prop.getPropertyValue(),
                        prop.getComputerHardware().getId()
                ))
                .sorted(Comparator.comparing(HardwarePropertyItem::getPropertyName))
                .toList();

        log.info("Retrieved {} properties for hardware model: {}", properties.size(), hardwareModel.getName());

        HardwareModelItem modelItem = new HardwareModelItem();
        modelItem.setProperties(properties);
        modelItem.setId(hardwareModel.getId());
        modelItem.setName(hardwareModel.getName());
        modelItem.setType(hardwareModel.getHardwareType());

        return ResponseEntity.ok(modelItem);
    }

}
