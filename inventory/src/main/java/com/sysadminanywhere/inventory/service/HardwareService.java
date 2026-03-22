package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.wmi.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HardwareService {

    private final WmiServiceClient wmiServiceClient;

    public HardwareService(WmiServiceClient wmiServiceClient) {
        this.wmiServiceClient = wmiServiceClient;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transactional
    public void scanHardware(Computer computer) {
        String hostName = computer.getName();
        log.info("Scanning hardware on computer {}", hostName);

        List<Map<String, Object>> diskDrives = execute(hostName, "SELECT * FROM Win32_DiskDrive");
        List<Map<String, Object>> operatingSystems = execute(hostName, "SELECT * FROM Win32_OperatingSystem");
        List<Map<String, Object>> diskPartitions = execute(hostName, "SELECT * FROM Win32_DiskPartition");
        List<Map<String, Object>> processors = execute(hostName, "SELECT * FROM Win32_Processor");
        List<Map<String, Object>> videoControllers = execute(hostName, "SELECT * FROM Win32_VideoController");
        List<Map<String, Object>> physicalMemory = execute(hostName, "SELECT * FROM Win32_PhysicalMemory");
        List<Map<String, Object>> logicalDisks = execute(hostName, "SELECT * FROM Win32_LogicalDisk");
        List<Map<String, Object>> baseBoards = execute(hostName, "SELECT * FROM Win32_BaseBoard");
        List<Map<String, Object>> bios = execute(hostName, "SELECT * FROM Win32_BIOS");
        List<Map<String, Object>> computerSystems = execute(hostName, "SELECT * FROM Win32_ComputerSystem");
    }

    private List<Map<String, Object>> execute(String hostName, String query) {
        List<Map<String, Object>> list = null;

        try {
            var response = wmiServiceClient.execute(new ExecuteDto(hostName, query));

            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to execute WMI query on computer {}: HTTP {}",
                        hostName, response != null ? response.getStatusCode() : "NULL");
            }

            list = (List<Map<String, Object>>) response.getBody();
        } catch (Exception ex) {
            log.error("Failed to execute WMI query on computer {}: {}", hostName, ex.getMessage());
        }

        if (list == null) {
            log.error("WMI client returned null for host {}", hostName);
        }

        return list;
    }

}