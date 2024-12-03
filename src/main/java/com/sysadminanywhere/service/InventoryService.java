package com.sysadminanywhere.service;

import com.sysadminanywhere.client.InventoryClient;
import com.sysadminanywhere.client.dto.SoftwareCount;
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

}