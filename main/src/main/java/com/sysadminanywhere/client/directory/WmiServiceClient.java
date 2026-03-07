package com.sysadminanywhere.client.directory;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "wmi",
        url = "${app.services.directory.uri}",
        configuration = FeignConfiguration.class
)
public interface WmiServiceClient {

    @PostMapping(value = "/api/wmi/execute")
    ResponseEntity<?> execute(@RequestBody ExecuteDto executeDto);

    @PostMapping("/api/wmi/execute/clear")
    ResponseEntity<?> clearExecuteCache(@RequestBody ExecuteDto executeDto);

    @PostMapping("/api/wmi/invoke")
    ResponseEntity<?> invoke(@RequestBody InvokeDto invokeDto);

    @PostMapping("/api/wmi/command")
    ResponseEntity<?> command(@RequestBody CommandDto commandDto);

}
