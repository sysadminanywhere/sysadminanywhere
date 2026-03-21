package com.sysadminanywhere.inventory.controller;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.HardwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
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

    /**
     * Получение ПО на компьютере с постраничным выводом
     */
    @GetMapping("/computers/{computerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SoftwareOnComputer>> getSoftwareOnComputer(
            @PathVariable Long computerId,
            Pageable pageable) {

        Page<SoftwareOnComputer> result = softwareRepository.getSoftwareOnComputer(computerId, pageable);
        log.info("Retrieved software for computer: {}", computerId);

        return ResponseEntity.ok(result);
    }

    /**
     * Получение оборудования на компьютере
     */
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

    /**
     * Получение типов оборудования
     */
    @GetMapping("/hardware/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getHardwareTypes() {
        try {
            List<String> types = hardwareRepository.findAllHardwareTypes();
            log.info("Retrieved {} hardware types", types.size());
            return ResponseEntity.ok(types);
        } catch (Exception ex) {
            log.error("Error retrieving hardware types: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Поиск оборудования
     */
    @GetMapping("/hardware/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Hardware>> searchHardware(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            Pageable pageable) {

        try {
            Page<Hardware> result = hardwareRepository.searchHardware(name, type, pageable);
            log.info("Retrieved {} hardware items with filters name={}, type={}", 
                    result.getTotalElements(), name, type);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error searching hardware: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Запуск сканирования оборудования компьютера
     */
    @PostMapping("/computers/{computerId}/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> scanComputerHardware(
            @PathVariable Long computerId) {

        try {
            Computer computer = computerRepository.findById(computerId).orElse(null);
            if (computer == null) {
                log.warn("Computer not found: {}", computerId);
                return ResponseEntity.notFound().build();
            }

            inventoryService.scanHardware(computer.getName());
            log.info("Started hardware scan for computer: {}", computerId);
            return ResponseEntity.ok("Hardware scan started for computer: " + computer.getName());
        } catch (Exception ex) {
            log.error("Error starting hardware scan for computer {}: {}", computerId, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение статистики установок ПО с фильтрацией по названию и вендору
     */
    @GetMapping("/count")
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

    /**
     * Получение компьютеров с установленным ПО
     */
    @GetMapping("/{softwareId}")
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

    /**
     * Получение детальной информации об оборудовании
     */
    @GetMapping("/hardware/{hardwareId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hardware> getHardwareDetails(@PathVariable Long hardwareId) {
        try {
            return hardwareRepository.findById(hardwareId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception ex) {
            log.error("Error retrieving hardware details for {}: {}", hardwareId, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Удаление записи об оборудовании компьютера
     */
    @DeleteMapping("/computers/{computerId}/hardware/{hardwareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeHardwareFromComputer(
            @PathVariable Long computerId,
            @PathVariable Long hardwareId) {

        try {
            Computer computer = computerRepository.findById(computerId).orElse(null);
            Hardware hardware = hardwareRepository.findById(hardwareId).orElse(null);

            if (computer == null || hardware == null) {
                log.warn("Computer or hardware not found: computer={}, hardware={}", computerId, hardwareId);
                return ResponseEntity.notFound().build();
            }

            List<ComputerHardware> links = computerHardwareRepository
                    .findAllByComputerAndHardware(computer, hardware);

            if (!links.isEmpty()) {
                computerHardwareRepository.delete(links.get(0));
                log.info("Removed hardware {} from computer {}", hardwareId, computerId);
                return ResponseEntity.ok("Hardware removed from computer");
            }

            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error removing hardware from computer {}: {}", computerId, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение статистики по оборудованию
     */
    @GetMapping("/hardware/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHardwareStatistics() {
        try {
            Map<String, Object> stats = Map.of(
                    "totalHardware", hardwareRepository.count(),
                    "totalComputerHardware", computerHardwareRepository.count(),
                    "lastScanTime", LocalDateTime.now()
            );

            log.info("Retrieved hardware statistics");
            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            log.error("Error retrieving hardware statistics: {}", ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Получение количества оборудования с постраничным выводом
     */
    @GetMapping("/hardware/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<HardwareCount>> getHardwareCount(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            Pageable pageable) {

        Map<String, String> filters = Map.of();
        if (name != null) filters = new HashMap<>(filters);
        if (type != null) filters = new HashMap<>(filters);
        
        Page<HardwareCount> result = inventoryService.getHardwareCount(pageable, filters);
        log.info("Retrieved hardware count");

        return ResponseEntity.ok(result);
    }

    /**
     * Получение компьютеров с определенным оборудованием
     */
    @GetMapping("/hardware/{hardwareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ComputerItem>> getComputersWithHardware(
            @PathVariable Long hardwareId,
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Map<String, String> filters = Map.of();
        if (name != null) filters = new HashMap<>(filters);
        
        Page<ComputerItem> result = inventoryService.getComputersWithHardware(hardwareId, pageable, filters);
        log.info("Retrieved computers with hardware: {}", hardwareId);

        return ResponseEntity.ok(result);
    }

    /**
     * Получение всех компьютеров с проверенным оборудованием
     */
    @GetMapping("/hardware/computers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ComputerItem>> getAllComputersWithHardware(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        log.info("Request received for getAllComputersWithHardware. Name filter: '{}', Pageable: {}", name, pageable);

        Map<String, String> filters = Map.of();
        if (name != null && !name.isEmpty()) {
            filters = new HashMap<>();
            filters.put("name", name);
        }
        
        Page<ComputerItem> result = inventoryService.getAllComputersWithHardware(pageable, filters);
        log.info("Returning {} computers with hardware inventory", result.getTotalElements());

        return ResponseEntity.ok(result);
    }

    /**
     * Временный endpoint для отладки - проверка количества записей в таблицах
     */
    @GetMapping("/debug/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> debugCounts() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("computers", computerRepository.count());
        counts.put("hardware", hardwareRepository.count());
        counts.put("computerHardware", computerHardwareRepository.count());
        
        log.info("Debug counts: {}", counts);
        return ResponseEntity.ok(counts);
    }

}
