package com.sysadminanywhere.client.inventory;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.inventory.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface InventoryServiceClient {

    // Software

    @GetExchange("/api/inventory/software/count")
    PageResponse<SoftwareCount> getSoftwareCount(@RequestParam String name, @RequestParam String vendor, @RequestParam int page, @RequestParam int size, @RequestParam String sort);

    @GetExchange("/api/inventory/computers/{computerId}/software")
    PageResponse<SoftwareOnComputer> getSoftwareOnComputer(@PathVariable Long computerId, @RequestParam int page, @RequestParam int size, @RequestParam String sort);

    @GetExchange("/api/inventory/software/{softwareId}")
    PageResponse<ComputerItem> getComputersWithSoftware(@PathVariable Long softwareId, @RequestParam String name, @RequestParam int page, @RequestParam int size, @RequestParam String sort);


    // Hardware

    @GetExchange("/api/inventory/hardware/count")
    Page<Object[]> getHardwareCount(@RequestParam String name, @RequestParam String type, @RequestParam int page, @RequestParam int size, @RequestParam String sort);

    @GetExchange("/api/inventory/hardware/{hardwareId}")
    HardwareModelItem getHardwareModelProperties(@PathVariable Long hardwareId);

    @GetExchange("/api/inventory/hardware")
    PageResponse<HardwareItem> getHardware(@RequestParam String name, @RequestParam String type, @RequestParam int page, @RequestParam int size, @RequestParam String sort);


    // Ping

    @GetExchange("/ping")
    String ping();

}
