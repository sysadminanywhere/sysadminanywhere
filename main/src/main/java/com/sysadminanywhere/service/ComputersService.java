package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.ComputersServiceClient;
import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.model.*;
import com.sysadminanywhere.model.wmi.*;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ComputersService {

    private final LdapService ldapService;
    private final WmiService wmiService;
    private final ComputersServiceClient computersServiceClient;

    public ComputersService(LdapService ldapService, WmiService wmiService, ComputersServiceClient computersServiceClient) {
        this.ldapService = ldapService;
        this.wmiService = wmiService;
        this.computersServiceClient = computersServiceClient;
    }

    @SneakyThrows
    public Page<ComputerEntry> getAll(Pageable pageable, String filters, String... attributes) {
        return computersServiceClient.getAll(pageable, filters, attributes);
    }

    public List<ComputerEntry> getAll(String filters, String... attributes) {
        return computersServiceClient.getList(filters, attributes);
    }

    public List<ComputerEntry> getAll() {
        List<EntryDto> list = ldapService.searchWithAttributes("(objectClass=computer)",
                "cn", "useraccountcontrol", "operatingsystem", "primarygroupid");

        List<ComputerEntry> items = new ArrayList<>();

        for (EntryDto entryDto : list) {
            ComputerEntry item = new ComputerEntry();
            item.setCn(entryDto.getAttributes().get("cn").toString());
            item.setUserAccountControl(Integer.parseInt(entryDto.getAttributes().get("useraccountcontrol").toString()));
            item.setPrimaryGroupId(Integer.parseInt(entryDto.getAttributes().get("primarygroupid").toString()));
            item.setOperatingSystem("");

            if (entryDto.getAttributes().containsKey("operatingsystem")) {
                item.setOperatingSystem(entryDto.getAttributes().get("operatingsystem").toString());
            }

            items.add(item);
        }

        return items;
    }

    public ComputerEntry getByCN(String cn) {
        return computersServiceClient.getByCN(cn);
    }

    @SneakyThrows
    public ComputerEntry add(String distinguishedName, ComputerEntry computer, boolean isEnabled) {
        return computersServiceClient.add(new AddComputerDto(distinguishedName,computer, isEnabled));
    }

    public ComputerEntry update(ComputerEntry computer) {
        return computersServiceClient.update(computer);
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        computersServiceClient.delete(distinguishedName);
    }

    public String getDefaultContainer() {
        return ldapService.getComputersContainer();
    }


    // WMI =================================

    public Page<ProcessEntity> getProcesses(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            String query = "Select * From Win32_Process" + getWmiQueryFromFilters(filters);
            WmiResolveService<ProcessEntity> wmiResolveService = new WmiResolveService<>(ProcessEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, query), pageable);
        } catch (Exception ex) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public void clearProcesses(Map<String, String> filters, String hostName) {
        String query = "Select * From Win32_Process" + getWmiQueryFromFilters(filters);
        wmiService.clearExecuteCache(hostName, query);
    }

    public Page<ServiceEntity> getServices(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            String query = "Select * From Win32_Service" + getWmiQueryFromFilters(filters);
            WmiResolveService<ServiceEntity> wmiResolveService = new WmiResolveService<>(ServiceEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, query), pageable);
        } catch (Exception ex) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public void clearServices(Map<String, String> filters, String hostName) {
        String query = "Select * From Win32_Service" + getWmiQueryFromFilters(filters);
        wmiService.clearExecuteCache(hostName, query);
    }

    public Page<SoftwareEntity> getSoftware(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            String query = "Select * From Win32_Product" + getWmiQueryFromFilters(filters);
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, query), pageable);
        } catch (Exception ex) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<SoftwareEntity> getSoftware(String hostName) {
        try {
            String query = "Select * From Win32_Product";
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, query));
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public Page<EventEntity> getEvents(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            WmiResolveService<EventEntity> wmiResolveService = new WmiResolveService<>(EventEntity.class);

            String queryString = getEventQueryString(filters);

            return wmiResolveService.getValues(wmiService.execute(hostName, queryString), pageable);
        } catch (Exception ex) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    private String getEventQueryString(Map<String, String> filters) {
        LocalDate date = LocalDate.parse(filters.get("date"));

        String startDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "000000.000000000";
        String endDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "235959.000000000";

        String queryString = "Select RecordNumber, EventType, EventCode, Type, TimeGenerated, SourceName, Category, Logfile, Message From Win32_NTLogEvent";

        queryString += " Where TimeGenerated >= '" + startDate + "' AND TimeGenerated <= '" + endDate + "'";

        if (!filters.get("sourceName").toString().isEmpty())
            queryString += " AND sourceName LIKE '" + filters.get("sourceName").toString() + "%'";

        String eventType = filters.get("eventType").toString();
        switch (eventType) {
            case "Error":
                queryString += " And EventType = 1";
                break;
            case "Warning":
                queryString += " And EventType = 2";
                break;
            case "Information":
                queryString += " And EventType = 3";
                break;
            case "Audit Success":
                queryString += " And EventType = 4";
                break;
            case "Audit Failure":
                queryString += " And EventType = 5";
                break;
        }

        return queryString;
    }

    public void clearEvents(Map<String, String> filters, String hostName) {
        wmiService.clearExecuteCache(hostName, getEventQueryString(filters));
    }

//    public Page<HardwareEntity> getHardware(Pageable pageable, String hostName) {
//        try {
//            return null;
//        } catch (Exception ex) {
//            Notification notification = Notification.show(ex.getMessage());
//            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//            return new PageImpl<>(new ArrayList<>(), pageable, 0);
//        }
//    }

    public List<DiskDriveEntity> getDiskDrive(String hostName) {
        try {
            WmiResolveService<DiskDriveEntity> wmiResolveService = new WmiResolveService<>(DiskDriveEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_DiskDrive"));
        } catch (Exception ex) {
            return null;
        }
    }

    public OperatingSystemEntity getOperatingSystem(String hostName) {
        try {
            WmiResolveService<OperatingSystemEntity> wmiResolveService = new WmiResolveService<>(OperatingSystemEntity.class);
            return wmiResolveService.getValue(wmiService.execute(hostName, "SELECT * FROM Win32_OperatingSystem").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<DiskPartitionEntity> getDiskPartition(String hostName) {
        try {
            WmiResolveService<DiskPartitionEntity> wmiResolveService = new WmiResolveService<>(DiskPartitionEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_DiskPartition"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<ProcessorEntity> getProcessor(String hostName) {
        try {
            WmiResolveService<ProcessorEntity> wmiResolveService = new WmiResolveService<>(ProcessorEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_Processor"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<VideoControllerEntity> getVideoController(String hostName) {
        try {
            WmiResolveService<VideoControllerEntity> wmiResolveService = new WmiResolveService<>(VideoControllerEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_VideoController"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<PhysicalMemoryEntity> getPhysicalMemory(String hostName) {
        try {
            WmiResolveService<PhysicalMemoryEntity> wmiResolveService = new WmiResolveService<>(PhysicalMemoryEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_PhysicalMemory"));
        } catch (Exception ex) {
            return null;
        }
    }

    public List<LogicalDiskEntity> getLogicalDisk(String hostName) {
        try {
            WmiResolveService<LogicalDiskEntity> wmiResolveService = new WmiResolveService<>(LogicalDiskEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "SELECT * FROM Win32_LogicalDisk"));
        } catch (Exception ex) {
            return null;
        }
    }

    public BaseboardEntity getBaseBoard(String hostName) {
        try {
            WmiResolveService<BaseboardEntity> wmiResolveService = new WmiResolveService<>(BaseboardEntity.class);
            return wmiResolveService.getValue(wmiService.execute(hostName, "SELECT * FROM Win32_BaseBoard").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public BIOSEntity getBIOS(String hostName) {
        try {
            WmiResolveService<BIOSEntity> wmiResolveService = new WmiResolveService<>(BIOSEntity.class);
            return wmiResolveService.getValue(wmiService.execute(hostName, "SELECT * FROM Win32_BIOS").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public ComputerSystemEntity getComputerSystem(String hostName) {
        try {
            WmiResolveService<ComputerSystemEntity> wmiResolveService = new WmiResolveService<>(ComputerSystemEntity.class);
            return wmiResolveService.getValue(wmiService.execute(hostName, "SELECT * FROM Win32_ComputerSystem").get(0));
        } catch (Exception ex) {
            return null;
        }
    }

    public void reboot(String hostName) {
        Map<String, Object> inputMap = Collections.singletonMap("Flags", 6);
        try {
            wmiService.invoke(hostName, "Win32_OperatingSystem=@", "Win32_OperatingSystem", "Win32Shutdown", inputMap);
        } catch (Exception ex) {
        }
    }

    public void shutdown(String hostName) {
        Map<String, Object> inputMap = Collections.singletonMap("Flags", 12);
        try {
            wmiService.invoke(hostName, "Win32_OperatingSystem=@", "Win32_OperatingSystem", "Win32Shutdown", inputMap);
        } catch (Exception ex) {
        }
    }

    public Integer getProcessorLoad(String hostName) {
        try {
            List<Map<String, Object>> result = wmiService.execute(hostName, "Select * FROM Win32_PerfFormattedData_PerfOS_Processor");

            for (Map<String, Object> item : result) {
                if (item.get("Name").toString().equals("_Total")) {
                    return Integer.valueOf(item.get("PercentProcessorTime").toString());
                }
            }
        } catch (Exception ex) {
            return 0;
        }
        return 0;
    }

    public Long getAvailableBytes(String hostName) {
        try {
            List<Map<String, Object>> result = wmiService.execute(hostName, "Select * FROM Win32_PerfFormattedData_PerfOS_Memory");
            return Long.valueOf(result.get(0).get("AvailableBytes").toString());
        } catch (Exception ex) {
            return 0L;
        }
    }

    public Long getTotalPhysicalMemory(String hostName) {
        try {
            List<Map<String, Object>> result = wmiService.execute(hostName, "Select * FROM Win32_ComputerSystem");
            return Long.valueOf(result.get(0).get("TotalPhysicalMemory").toString());
        } catch (Exception ex) {
            return 0L;
        }
    }

    public Integer getDisk(String hostName) {
        try {
            List<Map<String, Object>> result = wmiService.execute(hostName, "Select * From Win32_LogicalDisk WHERE Caption='C:'");

            Long diskSize = Long.valueOf(result.get(0).get("Size").toString());
            Long diskFreeSpace = Long.valueOf(result.get(0).get("FreeSpace").toString());

            Long percent = ((diskSize - diskFreeSpace) * 100) / diskSize;

            return percent.intValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    private String getWmiQueryFromFilters(Map<String, String> filters) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                list.add(entry.getKey() + " LIKE '" + entry.getValue() + "%'");
            }
        }

        if (list.isEmpty())
            return "";
        else
            return " WHERE " + String.join(" AND ", list);
    }

    public LdapService getLdapService() {
        return ldapService;
    }

    public void stopProcess(String hostName, ProcessEntity process) {
        try {
            Map<String, Object> inputMap = Collections.singletonMap("Reason", 1);
            Map<String, Object> result = wmiService.invoke(hostName, String.format("Win32_Process.Handle='%s'", process.getHandle()), "Win32_Process", "Terminate", inputMap);
            if (result.containsKey("ReturnValue") && result.get("ReturnValue") != null) {
                if ((int) result.get("ReturnValue") > 0) {
                    String errorMessage = switch ((int) result.get("ReturnValue")) {
                        case 2 -> "Access denied";
                        case 3 -> "Insufficient privilege";
                        case 8 -> "Unknown failure";
                        case 9 -> "Path not found";
                        case 21 -> "Invalid parameter";
                        default -> "Other error";
                    };
                }
            }
        } catch (Exception e) {
        }
    }

    public void startService(String hostName, ServiceEntity service) {
        try {
            wmiService.executeCommand(hostName, String.format("net start '%s'", service.getName()), "c:\\");
        } catch (Exception ex) {
        }
    }

    public void stopService(String hostName, ServiceEntity service) {
        try {
            wmiService.executeCommand(hostName, String.format("net stop '%s'", service.getName()), "c:\\");
        } catch (Exception ex) {
        }
    }

}