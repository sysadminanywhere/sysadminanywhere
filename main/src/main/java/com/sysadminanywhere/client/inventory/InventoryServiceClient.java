package com.sysadminanywhere.client.inventory;

import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "inventory",
        url = "${app.services.inventory.uri}",
        configuration = FeignClientConfig.class
)
public interface InventoryServiceClient {

    @PostMapping("/computers/{computerId}")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@PathVariable Long computerId, @RequestBody Map<String, String> filters, Pageable pageable);

    @PostMapping("/count")
    Page<SoftwareCount> getSoftwareCount(@RequestBody Map<String, String> filters, Pageable pageable);

    @PostMapping("/{softwareId}")
    Page<ComputerItem> getComputersWithSoftware(@PathVariable Long softwareId, @RequestBody Map<String, String> filters, Pageable pageable);

}