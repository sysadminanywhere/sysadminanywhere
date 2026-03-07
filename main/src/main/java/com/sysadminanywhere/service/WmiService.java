package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.WmiServiceClient;
import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WmiService {

    private final WmiServiceClient wmiServiceClient;

    public WmiService(WmiServiceClient wmiServiceClient) {
        this.wmiServiceClient = wmiServiceClient;
    }

    /**
     * Выполнение WMI запроса
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> execute(String hostName, String query) {
        try {
            ExecuteDto dto = new ExecuteDto(hostName, query);
            ResponseEntity<?> response = wmiServiceClient.execute(dto);

            if (response.getStatusCode().is2xxSuccessful()) {
                return (List<Map<String, Object>>) response.getBody();
            } else {
                log.error("Error executing WMI query: {}", response.getStatusCode());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error executing WMI query for host: {}", hostName, e);
            return List.of();
        }
    }

    /**
     * Очистка кэша WMI запроса
     */
    public void clearExecuteCache(String hostName, String query) {
        try {
            wmiServiceClient.clearExecuteCache(new ExecuteDto(hostName, query));
            log.info("Cache cleared for host: {}", hostName);
        } catch (Exception e) {
            log.error("Error clearing cache for host: {}", hostName, e);
        }
    }

    /**
     * Вызов WMI метода
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> invoke(String hostName, String path, String className, String methodName, Map<String, Object> inputMap) {
        try {
            ResponseEntity<?> response = wmiServiceClient.invoke(new InvokeDto(hostName, path, className, methodName, inputMap));

            if (response.getStatusCode().is2xxSuccessful()) {
                return (Map<String, Object>) response.getBody();
            } else {
                log.error("Error invoking WMI method: {}", response.getStatusCode());
                return Map.of();
            }
        } catch (Exception e) {
            log.error("Error invoking WMI method on host: {}", hostName, e);
            return Map.of();
        }
    }

    /**
     * Выполнение системной команды
     */
    public void executeCommand(String hostName, String command, String workingDirectory) {
        try {
            wmiServiceClient.command(new CommandDto(hostName, command, workingDirectory));
            log.info("Command executed on host: {}", hostName);
        } catch (Exception e) {
            log.error("Error executing command on host: {}", hostName, e);
        }
    }

}