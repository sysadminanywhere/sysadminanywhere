package com.sysadminanywhere.client.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.HardwareItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "inventory",
        url = "${app.services.inventory.uri}",
        configuration = FeignConfiguration.class
)
public interface InventoryServiceClient {

    // Software

    @GetMapping("/api/inventory/software/count")
    Page<SoftwareCount> getSoftwareCount(@RequestParam("name") String name, @RequestParam("vendor") String vendor, Pageable pageable);

    @GetMapping("/api/inventory/computers/{computerId}/software")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@PathVariable Long computerId, Pageable pageable);

    @GetMapping("/api/inventory/software/{softwareId}")
    Page<ComputerItem> getComputersWithSoftware(@PathVariable Long softwareId, @RequestParam("name") String name, Pageable pageable);


    // Hardware

    @GetMapping("/api/inventory/hardware/count")
    Page<Object[]> getHardwareCount(@RequestParam("name") String name, @RequestParam("type") String type, Pageable pageable);

    @GetMapping("/api/inventory/hardware/{hardwareId}")
    Page<ComputerItem> getComputersWithHardware(@PathVariable Long hardwareId, @RequestParam("name") String name, Pageable pageable);

    @GetMapping("/api/inventory/hardware")
    Page<HardwareItem> getHardware(@RequestParam("name") String name, @RequestParam("type") String type, Pageable pageable);


    // Ping

    @GetMapping("/ping")
    String ping();

}