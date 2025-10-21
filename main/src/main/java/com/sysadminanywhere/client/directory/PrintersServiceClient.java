package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.PrinterEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "printers",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface PrintersServiceClient {

    @GetMapping("/api/printers")
    Page<PrinterEntry> getAll(Pageable pageable, @RequestParam("filters") String filters);

    @GetMapping("/api/printers/list")
    List<PrinterEntry> getList(@RequestParam("filters") String filters);

    @GetMapping("/api/printers/{cn}")
    PrinterEntry getByCN(@PathVariable("cn") String cn);

    @DeleteMapping("/api/printers")
    void delete(@RequestParam("distinguishedName") String distinguishedName);

}