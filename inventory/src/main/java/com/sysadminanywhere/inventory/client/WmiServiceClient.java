package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface WmiServiceClient {

    @PostExchange(value = "/api/wmi/execute")
    ResponseEntity<?> execute(@RequestBody ExecuteDto executeDto);

    @PostExchange("/api/wmi/execute/clear")
    ResponseEntity<?> clearExecuteCache(@RequestBody ExecuteDto executeDto);

    @PostExchange("/api/wmi/invoke")
    ResponseEntity<?> invoke(@RequestBody InvokeDto invokeDto);

    @PostExchange("/api/wmi/command")
    ResponseEntity<?> command(@RequestBody CommandDto commandDto);

}
