package com.sysadminanywhere.service;

import com.sysadminanywhere.model.*;
import com.sysadminanywhere.model.hardware.DiskDriveEntity;
import com.sysadminanywhere.model.hardware.HardwareEntity;
import com.sysadminanywhere.model.hardware.OperatingSystemEntity;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.sentrysoftware.wmi.exceptions.WmiComException;
import org.sentrysoftware.wmi.exceptions.WqlQuerySyntaxException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class ComputersService {

    private final LdapService ldapService;
    private final WmiService wmiService;

    ResolveService<ComputerEntry> resolveService = new ResolveService<>(ComputerEntry.class);

    public ComputersService(LdapService ldapService, WmiService wmiService) {
        this.ldapService = ldapService;
        this.wmiService = wmiService;
    }

    @SneakyThrows
    public Page<ComputerEntry> getAll(Pageable pageable, String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=computer)" + filters + ")", pageable.getSort());
        return resolveService.getADPage(result, pageable);
    }

    public List<ComputerEntry> getAll(String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=computer)" + filters + ")");
        return resolveService.getADList(result);
    }

    public ComputerEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=computer)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        return entry.map(attributes -> resolveService.getADValue(attributes)).orElse(null);
    }

    @SneakyThrows
    public ComputerEntry add(String distinguishedName, ComputerEntry computer, boolean isEnabled) {
        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + computer.getCn() + "," + ldapService.getComputersContainer();
        } else {
            dn = "cn=" + computer.getCn() + "," + distinguishedName;
        }

        if (computer.getSamAccountName() == null || computer.getSamAccountName().isEmpty())
            computer.setSamAccountName(computer.getCn());

        Entry entry = new DefaultEntry(
                dn,
                "sAMAccountName", computer.getSamAccountName(),
                "objectClass:computer",
                "cn", computer.getCn()
        );

        ldapService.add(entry);

        ComputerEntry newComputer = getByCN(computer.getCn());

        int userAccountControl = newComputer.getUserAccountControl();

        if (!isEnabled) {
            if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue()) != UserAccountControls.ACCOUNTDISABLE.getValue())
                userAccountControl = userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue();
        } else {
            if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue()) == UserAccountControls.ACCOUNTDISABLE.getValue())
                userAccountControl = userAccountControl & ~UserAccountControls.ACCOUNTDISABLE.getValue();
        }

        ldapService.updateProperty(newComputer.getDistinguishedName(), "userAccountControl", String.valueOf(userAccountControl));

        if (computer.getDescription() != null && !computer.getDescription().isEmpty())
            ldapService.updateProperty(newComputer.getDistinguishedName(), "description", computer.getDescription());

        if (computer.getLocation() != null && !computer.getLocation().isEmpty())
            ldapService.updateProperty(newComputer.getDistinguishedName(), "location", computer.getLocation());

        return newComputer;
    }

    public ComputerEntry update(ComputerEntry computer) {
        ModifyRequest modifyRequest = resolveService.getModifyRequest(computer, getByCN(computer.getCn()));
        ldapService.update(modifyRequest);

        return getByCN(computer.getCn());
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public UserAccountControls getUserControl(int userAccountControl) {
        return UserAccountControls.fromValue(userAccountControl);
    }

    public String getDefaultContainer() {
        return ldapService.getComputersContainer();
    }

    public Page<ProcessEntity> getProcesses(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            WmiResolveService<ProcessEntity> wmiResolveService = new WmiResolveService<>(ProcessEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Process" + getWmiQueryFromFilters(filters)), pageable);
        } catch (WmiComException | WqlQuerySyntaxException | TimeoutException ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<ServiceEntity> getServices(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            WmiResolveService<ServiceEntity> wmiResolveService = new WmiResolveService<>(ServiceEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Service" + getWmiQueryFromFilters(filters)), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getSoftware(Pageable pageable, Map<String, String> filters, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Product" + getWmiQueryFromFilters(filters)), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<EventEntity> getEvents(Pageable pageable, Map<String, Object> filters, String hostName) {
        try {
            WmiResolveService<EventEntity> wmiResolveService = new WmiResolveService<>(EventEntity.class);

            LocalDate date = (LocalDate) filters.get("date");

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

            return wmiResolveService.GetValues(wmiService.execute(hostName, queryString), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<HardwareEntity> getHardware(Pageable pageable, String hostName) {
        try {
            return null;
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<DiskDriveEntity> getDiskDrive(Pageable pageable, String hostName) {
        try {
            WmiResolveService<DiskDriveEntity> wmiResolveService = new WmiResolveService<>(DiskDriveEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_DiskDrive"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<OperatingSystemEntity> getOperatingSystem(Pageable pageable, String hostName) {
        try {
            WmiResolveService<OperatingSystemEntity> wmiResolveService = new WmiResolveService<>(OperatingSystemEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_OperatingSystem"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getDiskPartition(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_DiskPartition"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getProcessor(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_Processor"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getVideoController(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_VideoController"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getPhysicalMemory(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_PhysicalMemory"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getLogicalDisk(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_LogicalDisk"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getBaseBoard(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_BaseBoard"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getBIOS(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_BIOS"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getComputerSystem(Pageable pageable, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "SELECT * FROM Win32_ComputerSystem"), pageable);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public void reboot(String hostName) {
        Map<String, Object> inputMap = Collections.singletonMap("Flags", 6);
        try {
            wmiService.invoke(hostName, "Win32_OperatingSystem=@", "Win32_OperatingSystem", "Win32Shutdown", inputMap);
        } catch (WmiComException ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void shutdown(String hostName) {
        Map<String, Object> inputMap = Collections.singletonMap("Flags", 12);
        try {
            wmiService.invoke(hostName, "Win32_OperatingSystem=@", "Win32_OperatingSystem", "Win32Shutdown", inputMap);
        } catch (WmiComException ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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
                    Notification notification = Notification.show(errorMessage);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else {
                    Notification notification = Notification.show("Process '" + process.getCaption() + "' stopped");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            }
        } catch (WmiComException e) {
            Notification notification = Notification.show(e.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

}