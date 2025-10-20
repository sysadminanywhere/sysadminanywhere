package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "computers",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface ComputersServiceClient {

    @GetMapping("/api/computers")
    Page<ComputerEntry> getAll(Pageable pageable, @RequestParam("filters") String filters);

}