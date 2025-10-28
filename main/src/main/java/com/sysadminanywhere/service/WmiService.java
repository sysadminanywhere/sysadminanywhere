package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.WmiServiceClient;
import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WmiService {

    private final WmiServiceClient wmiServiceClient;

    public WmiService(WmiServiceClient wmiServiceClient) {
        this.wmiServiceClient = wmiServiceClient;
    }

    public List<Map<String, Object>> execute(String hostName, String query) {
        ExecuteDto dto = new ExecuteDto(hostName, query);
        return wmiServiceClient.execute(dto);
    }

    public void clearExecuteCache(String hostName, String query) {
        wmiServiceClient.clearExecuteCache(new ExecuteDto(hostName, query));
    }

    public Map<String, Object> invoke(String hostName, String path, String className, String methodName, Map<String, Object> inputMap) {
        return wmiServiceClient.invoke(new InvokeDto(hostName, path, className, methodName, inputMap));
    }

    public void executeCommand(String hostName, String command, String workingDirectory) {
        wmiServiceClient.command(new CommandDto(hostName, command, workingDirectory));
    }

}