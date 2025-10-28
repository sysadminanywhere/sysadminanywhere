package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "wmi",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface WmiServiceClient {

    @PostMapping("/api/wmi/execute")
    List<Map<String, Object>> execute(@RequestBody ExecuteDto executeDto);

}
