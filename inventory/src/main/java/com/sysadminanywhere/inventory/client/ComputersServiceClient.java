package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.inventory.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "computers",
        url = "${app.services.directory.uri}",
        configuration = FeignConfiguration.class
)
public interface ComputersServiceClient {

    @GetMapping("/api/computers")
    Page<ComputerEntry> getAll(Pageable pageable, @RequestParam("filters") String filters, @RequestParam("attributes") String[] attributes);

    @GetMapping("/api/computers/list")
    List<ComputerEntry> getList(@RequestParam("filters") String filters, @RequestParam("attributes") String... attributes);

    @GetMapping("/api/computers/{cn}")
    ComputerEntry getByCN(@PathVariable("cn") String cn);

    @PostMapping("/api/computers")
    ComputerEntry add(@RequestBody AddComputerDto addComputer);

    @PutMapping("/api/computers")
    ComputerEntry update(@RequestBody ComputerEntry computer);

    @DeleteMapping("/api/computers")
    void delete(@RequestParam("distinguishedName") String distinguishedName);

}