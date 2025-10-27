package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.wmi.SoftwareEntity;
import com.sysadminanywhere.inventory.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "wmi",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface WmiServiceClient {

    @GetMapping("/api/wmi/software")
    List<SoftwareEntity> getSoftware(@PathVariable String hostName);

}
