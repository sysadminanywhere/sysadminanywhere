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
