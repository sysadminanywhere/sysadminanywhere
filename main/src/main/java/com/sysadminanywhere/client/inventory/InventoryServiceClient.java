package com.sysadminanywhere.client.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "inventory",
        url = "${app.services.inventory.uri}",
        configuration = FeignClientConfig.class
)
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/computers/{computerId}")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@PathVariable Long computerId, Pageable pageable);

    @GetMapping("/api/inventory/count")
    Page<SoftwareCount> getSoftwareCount(@RequestParam("name") String name, @RequestParam("vendor") String vendor, Pageable pageable);

    @GetMapping("/api/inventory/{softwareId}")
    Page<ComputerItem> getComputersWithSoftware(@PathVariable Long softwareId, @RequestParam("name") String name, Pageable pageable);

}