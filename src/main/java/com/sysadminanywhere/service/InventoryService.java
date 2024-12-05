package com.sysadminanywhere.service;

import com.sysadminanywhere.client.InventoryClient;
import com.sysadminanywhere.client.dto.ComputerDto;
import com.sysadminanywhere.client.dto.SoftwareCount;
import com.sysadminanywhere.client.dto.SoftwareOnComputer;
import com.sysadminanywhere.views.inventory.InventoryComputersWithSoftwareView;
import com.sysadminanywhere.views.inventory.InventorySoftwareView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryClient inventoryClient;

    public InventoryService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, InventorySoftwareView.Filters filters) {
        return inventoryClient.getSoftwareCount(pageable);
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable, InventorySoftwareView.Filters filters) {
        return inventoryClient.getSoftwareOnComputer(computerId, pageable);
    }

    public Page<ComputerDto> getComputersWithSoftware(Long softwareId, Pageable pageable, InventoryComputersWithSoftwareView.Filters filters) {
        return inventoryClient.getComputersWithSoftware(softwareId, pageable);
    }

}