package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "wmi",
        url = "${app.services.directory.uri}",
        configuration = FeignClientConfig.class
)
public interface WmiServiceClient {

    @PostMapping(value = "/api/wmi/execute")
    List<Map<String, Object>> execute(@RequestBody ExecuteDto executeDto);

    @PostMapping("/api/wmi/execute/clear")
    void clearExecuteCache(@RequestBody ExecuteDto executeDto);

    @PostMapping("/api/wmi/invoke")
    Map<String, Object> invoke(@RequestBody InvokeDto invokeDto);

    @PostMapping("/api/wmi/command")
    Boolean command(@RequestBody CommandDto commandDto);

}
