package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.inventory.controller.dto.ComputerDto;
import com.sysadminanywhere.inventory.controller.dto.SoftwareCount;
import com.sysadminanywhere.inventory.controller.dto.SoftwareOnComputer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory/software")
public class InventoryController {

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;

    public InventoryController(ComputerRepository computerRepository,
                               SoftwareRepository softwareRepository) {
        this.computerRepository = computerRepository;
        this.softwareRepository = softwareRepository;
    }

    @PostMapping("/computers/{computerId}")
    public ResponseEntity<Page<SoftwareOnComputer>> getSoftwareOnComputer(@PathVariable Long computerId, @RequestBody Map<String, String> filters, Pageable pageable) {
        return ResponseEntity.ok(softwareRepository.getSoftwareOnComputer(computerId, pageable));
    }

    @PostMapping("/count")
    public ResponseEntity<Page<SoftwareCount>> getSoftwareCount(@RequestBody Map<String, String> filters, Pageable pageable) {
        String name = filters.get("name") + "%";
        String vendor = filters.get("vendor") + "%";
        return ResponseEntity.ok(softwareRepository.getSoftwareInstallationCount(name, vendor, pageable));
    }

    @PostMapping("/{softwareId}")
    public ResponseEntity<Page<ComputerDto>> getComputersWithSoftware(@PathVariable Long softwareId, @RequestBody Map<String, String> filters, Pageable pageable) {
        String name = filters.get("name") + "%";
        return ResponseEntity.ok(computerRepository.getComputersWithSoftware(softwareId, name, pageable));
    }

}