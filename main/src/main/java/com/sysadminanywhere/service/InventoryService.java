package com.sysadminanywhere.service;

import com.sysadminanywhere.client.inventory.InventoryServiceClient;
import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.HardwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.model.wmi.HardwareEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {

    private final InventoryServiceClient inventoryServiceClient;

    public InventoryService(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable) {
        try {
            return inventoryServiceClient.getSoftwareOnComputer(computerId, pageable);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            String vendor = filters.get("vendor");
            return inventoryServiceClient.getSoftwareCount(name, vendor, pageable);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<HardwareCount> getHardwareCount(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            String type = filters.get("type");
            Page<Object[]> results = inventoryServiceClient.getHardwareCount(name, type, pageable);
            
            // Convert Object[] to HardwareCount
            List<HardwareCount> hardwareCounts = results.getContent().stream()
                .map(row -> new HardwareCount((Long) row[0], (String) row[1], (String) row[2], (Long) row[3]))
                .collect(Collectors.toList());
                
            return new PageImpl<>(hardwareCounts, pageable, results.getTotalElements());
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            return inventoryServiceClient.getComputersWithSoftware(softwareId, name, pageable);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<ComputerItem> getComputersWithHardware(Long hardwareId, Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            return inventoryServiceClient.getComputersWithHardware(hardwareId, name, pageable);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<ComputerItem> getAllComputersWithHardware(Pageable pageable, Map<String, String> filters) {
        try {
            String name = filters.get("name");
            return inventoryServiceClient.getAllComputersWithHardware(name, pageable);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Boolean ping() {
        try {
            inventoryServiceClient.ping();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private List<HardwareEntity> getHardware(String hostName) {
        return new ArrayList<>();
    }

}