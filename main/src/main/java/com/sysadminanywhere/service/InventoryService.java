package com.sysadminanywhere.service;

import com.sysadminanywhere.client.inventory.InventoryServiceClient;
import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.model.wmi.HardwareEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class InventoryService {

    private final InventoryServiceClient inventoryServiceClient;

    public InventoryService(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable) {
        return inventoryServiceClient.getSoftwareOnComputer(computerId, pageable);
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name");
        String vendor = filters.get("vendor");
        return inventoryServiceClient.getSoftwareCount(name, vendor, pageable);
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name");
        return inventoryServiceClient.getComputersWithSoftware(softwareId, name, pageable);
    }

    private List<HardwareEntity> getHardware(String hostName) {
        return new ArrayList<>();
    }

}