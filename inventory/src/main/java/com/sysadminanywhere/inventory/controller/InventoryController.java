package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import org.springframework.http.HttpStatus;
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
        return new ResponseEntity<>(softwareRepository.getSoftwareOnComputer(computerId, pageable), HttpStatus.OK);
    }

    @PostMapping("/count")
    public ResponseEntity<Page<SoftwareCount>> getSoftwareCount(@RequestBody Map<String, String> filters, Pageable pageable) {
        String name = filters.get("name") + "%";
        String vendor = filters.get("vendor") + "%";
        return new ResponseEntity<>(softwareRepository.getSoftwareInstallationCount(name, vendor, pageable), HttpStatus.OK);
    }

    @PostMapping("/{softwareId}")
    public ResponseEntity<Page<ComputerItem>> getComputersWithSoftware(@PathVariable Long softwareId, @RequestBody Map<String, String> filters, Pageable pageable) {
        String name = filters.get("name") + "%";
        return new ResponseEntity<>(computerRepository.getComputersWithSoftware(softwareId, name, pageable), HttpStatus.OK);
    }

}