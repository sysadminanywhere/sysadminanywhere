package com.sysadminanywhere.client.inventory;

import com.sysadminanywhere.common.inventory.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface InventoryServiceClient {

    // Software

    @GetExchange("/api/inventory/software/count")
    Page<SoftwareCount> getSoftwareCount(String name, String vendor, Pageable pageable);

    @GetExchange("/api/inventory/computers/{computerId}/software")
    Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable);

    @GetExchange("/api/inventory/software/{softwareId}")
    Page<ComputerItem> getComputersWithSoftware(Long softwareId, String name, Pageable pageable);


    // Hardware

    @GetExchange("/api/inventory/hardware/count")
    Page<Object[]> getHardwareCount(String name, String type, Pageable pageable);

    @GetExchange("/api/inventory/hardware/{hardwareId}")
    HardwareModelItem getHardwareModelProperties(Long hardwareId);

    @GetExchange("/api/inventory/hardware")
    Page<HardwareItem> getHardware(String name, String type, Pageable pageable);


    // Ping

    @GetExchange("/ping")
    String ping();

}
