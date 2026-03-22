package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.inventory.model.*;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.Hardware;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.HardwareRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.service.InventoryService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final ComputerHardwareRepository computerHardwareRepository;
    private final HardwareRepository hardwareRepository;
    private final InventoryService inventoryService;

    // Software

    @GetMapping("/software/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SoftwareCount>> getSoftwareCount(
            @RequestParam String name,
            @RequestParam String vendor,
            Pageable pageable) {

        name = name + "%";
        vendor = vendor + "%";

        Page<SoftwareCount> result = softwareRepository.getSoftwareInstallationCount(
                name, vendor, pageable);

        log.info("Retrieved software count for name: {}, vendor: {}", name, vendor);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/computers/{computerId}/software")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SoftwareOnComputer>> getSoftwareOnComputer(
            @PathVariable Long computerId,
            Pageable pageable) {

        Page<SoftwareOnComputer> result = softwareRepository.getSoftwareOnComputer(computerId, pageable);
        log.info("Retrieved software for computer: {}", computerId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/software/{softwareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ComputerItem>> getComputersWithSoftware(
            @PathVariable Long softwareId,
            @RequestParam String name,
            Pageable pageable) {

        name = name + "%";

        Page<ComputerItem> result = computerRepository.getComputersWithSoftware(
                softwareId, name, pageable);

        log.info("Retrieved computers with software: {}", softwareId);

        return ResponseEntity.ok(result);
    }


    // Hardware

    @GetMapping("/computers/{computerId}/hardware")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Hardware>> getHardwareOnComputer(
            @PathVariable Long computerId) {

        try {
            Computer computer = computerRepository.findById(computerId).orElse(null);
            if (computer == null) {
                log.warn("Computer not found: {}", computerId);
                return ResponseEntity.notFound().build();
            }

            List<ComputerHardware> computerHardwares = computerHardwareRepository.findAllByComputer(computer);
            List<Hardware> hardware = computerHardwares.stream()
                    .map(ComputerHardware::getHardware)
                    .toList();

            log.info("Retrieved {} hardware items for computer: {}", hardware.size(), computerId);
            return ResponseEntity.ok(hardware);
        } catch (Exception ex) {
            log.error("Error retrieving hardware for computer {}: {}", computerId, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hardware")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<HardwareItem>> searchHardware(
            @RequestParam(defaultValue = "") String name,
            @RequestParam String type,
            Pageable pageable) {

        try {
            Page<Hardware> result = hardwareRepository.searchHardware(name, type, pageable);
            Page<HardwareItem> hardwareItems = result.map(hardware -> 
                new HardwareItem(hardware.getId(), hardware.getName(), hardware.getType()));
            
            log.info("Retrieved {} hardware items with filters name='{}', type='{}'",
                    hardwareItems.getTotalElements(), name, type);
            return ResponseEntity.ok(hardwareItems);
        } catch (Exception ex) {
            log.error("Error searching hardware: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hardware/{hardwareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HardwareItem> getHardware(@PathVariable Long hardwareId) {
        try {
            return hardwareRepository.findById(hardwareId)
                    .map(hardware -> new HardwareItem(hardware.getId(), hardware.getName(), hardware.getType()))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception ex) {
            log.error("Error retrieving hardware for {}: {}", hardwareId, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
