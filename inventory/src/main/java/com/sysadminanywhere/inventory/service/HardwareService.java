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
    }

    public List<DiskDriveEntity> getDiskDrive(String hostName) {
        try {
            WmiResolveService<DiskDriveEntity> wmiResolveService = new WmiResolveService<>(DiskDriveEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_DiskDrive"));
        } catch (Exception ex) {
            return null;
        }
    }

    public OperatingSystemEntity getOperatingSystem(String hostName) {
        try {
            WmiResolveService<OperatingSystemEntity> wmiResolveService = new WmiResolveService<>(OperatingSystemEntity.class);
            return wmiResolveService.getValue(execute(hostName, "SELECT * FROM Win32_OperatingSystem").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<DiskPartitionEntity> getDiskPartition(String hostName) {
        try {
            WmiResolveService<DiskPartitionEntity> wmiResolveService = new WmiResolveService<>(DiskPartitionEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_DiskPartition"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<ProcessorEntity> getProcessor(String hostName) {
        try {
            WmiResolveService<ProcessorEntity> wmiResolveService = new WmiResolveService<>(ProcessorEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_Processor"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<VideoControllerEntity> getVideoController(String hostName) {
        try {
            WmiResolveService<VideoControllerEntity> wmiResolveService = new WmiResolveService<>(VideoControllerEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_VideoController"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<PhysicalMemoryEntity> getPhysicalMemory(String hostName) {
        try {
            WmiResolveService<PhysicalMemoryEntity> wmiResolveService = new WmiResolveService<>(PhysicalMemoryEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_PhysicalMemory"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<LogicalDiskEntity> getLogicalDisk(String hostName) {
        try {
            WmiResolveService<LogicalDiskEntity> wmiResolveService = new WmiResolveService<>(LogicalDiskEntity.class);
            return wmiResolveService.getValues(execute(hostName, "SELECT * FROM Win32_LogicalDisk"));
        } catch (Exception ex) {
            return null;
        }
    }

    public BaseboardEntity getBaseBoard(String hostName) {
        try {
            WmiResolveService<BaseboardEntity> wmiResolveService = new WmiResolveService<>(BaseboardEntity.class);
            return wmiResolveService.getValue(execute(hostName, "SELECT * FROM Win32_BaseBoard").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public BIOSEntity getBIOS(String hostName) {
        try {
            WmiResolveService<BIOSEntity> wmiResolveService = new WmiResolveService<>(BIOSEntity.class);
            return wmiResolveService.getValue(execute(hostName, "SELECT * FROM Win32_BIOS").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public ComputerSystemEntity getComputerSystem(String hostName) {
        try {
            WmiResolveService<ComputerSystemEntity> wmiResolveService = new WmiResolveService<>(ComputerSystemEntity.class);
            ComputerSystemEntity computerSystemEntity = wmiResolveService.getValue(execute(hostName, "SELECT * FROM Win32_ComputerSystem").get(0));
            return computerSystemEntity;
        } catch (Exception ex) {
            return null;
        }
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