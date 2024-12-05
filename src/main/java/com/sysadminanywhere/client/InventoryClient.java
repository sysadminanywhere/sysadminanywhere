package com.sysadminanywhere.client;

import com.sysadminanywhere.client.dto.ComputerDto;
import com.sysadminanywhere.client.dto.SoftwareCount;
import com.sysadminanywhere.client.dto.SoftwareOnComputer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "inventory", url = "${sysadminanywhere.inventory.address}")
public interface InventoryClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/inventory/software/count")
    Page<SoftwareCount> getSoftwareCount(Pageable pageable);

    @RequestMapping(method = RequestMethod.GET, value = "/api/inventory/software/computers/{computerId}")
    Page<SoftwareOnComputer> getSoftwareOnComputer(@PathVariable("computerId") Long computerId, Pageable pageable);

    @RequestMapping(method = RequestMethod.GET, value = "/api/inventory/software/{softwareId}")
    Page<ComputerDto> getComputersWithSoftware(@PathVariable("softwareId")Long softwareId, Pageable pageable);

}
