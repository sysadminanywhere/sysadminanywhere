package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.inventory.model.*;
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.HardwareModel;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.HardwareModelRepository;
import com.sysadminanywhere.inventory.repository.HardwarePropertyRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

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

    @GetMapping("/hardware")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<HardwareItem>> getHardwares(
            @RequestParam String name,
            @RequestParam String type,
            Pageable pageable) {

        name = name + "%";

        Page<HardwareItem> result = hardwareModelRepository.findByNameAndType(name, type, pageable);

        log.info("Retrieved hardwares: {}", result.getTotalElements());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/hardware/{hardwareId}")
    @PreAuthorize("hasRole('ADMIN')")
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
