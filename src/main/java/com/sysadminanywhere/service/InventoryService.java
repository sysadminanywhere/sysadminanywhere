package com.sysadminanywhere.service;

import com.sysadminanywhere.client.InventoryClient;
import com.sysadminanywhere.client.dto.ComputerDto;
import com.sysadminanywhere.client.dto.SoftwareCount;
import com.sysadminanywhere.client.dto.SoftwareOnComputer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InventoryService {

    private final InventoryClient inventoryClient;

    public InventoryService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        return inventoryClient.getSoftwareCount(filters, pageable);
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable, Map<String, String> filters) {
        return inventoryClient.getSoftwareOnComputer(computerId, filters, pageable);
    }

    public Page<ComputerDto> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        return inventoryClient.getComputersWithSoftware(softwareId, filters, pageable);
    }

}