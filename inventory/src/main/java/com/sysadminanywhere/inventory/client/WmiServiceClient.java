package com.sysadminanywhere.inventory.client;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.PostExchange;

public interface WmiServiceClient {

    @PostExchange(value = "/api/wmi/execute")
    ResponseEntity<?> execute(ExecuteDto executeDto);

    @PostExchange("/api/wmi/execute/clear")
    ResponseEntity<?> clearExecuteCache(ExecuteDto executeDto);

    @PostExchange("/api/wmi/invoke")
    ResponseEntity<?> invoke(InvokeDto invokeDto);

    @PostExchange("/api/wmi/command")
    ResponseEntity<?> command(CommandDto commandDto);

}
