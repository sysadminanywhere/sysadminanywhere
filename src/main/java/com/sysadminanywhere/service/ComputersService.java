package com.sysadminanywhere.service;

import com.sysadminanywhere.model.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.sentrysoftware.wmi.WmiHelper;
import org.sentrysoftware.wmi.wbem.WmiWbemServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

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

    public ComputerEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=computer)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        if (entry.isPresent())
            return resolveService.getADValue(entry.get());
        else
            return null;
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

    @SneakyThrows
    public Page<ProcessEntity> getProcesses(Pageable pageable, String filter, String hostName) {
        try {
            WmiResolveService<ProcessEntity> wmiResolveService = new WmiResolveService<>(ProcessEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Process"), pageable, filter);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @SneakyThrows
    public Page<ServiceEntity> getServices(Pageable pageable, String filter, String hostName) {
        try {
            WmiResolveService<ServiceEntity> wmiResolveService = new WmiResolveService<>(ServiceEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Service"), pageable, filter);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<SoftwareEntity> getSoftware(Pageable pageable, String filter, String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Product"), pageable, filter);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public Page<EventEntity> getEvents(Pageable pageable, String filter, String hostName) {
        try {
            WmiResolveService<EventEntity> wmiResolveService = new WmiResolveService<>(EventEntity.class);

            //"20240927000000.000000000"
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "000000.000000000";

            String queryString = "Select RecordNumber, EventType, EventCode, Type, TimeGenerated, SourceName, Category, Logfile, Message From Win32_NTLogEvent Where TimeGenerated > '" + today + "'";

            return wmiResolveService.GetValues(wmiService.execute(hostName, queryString), pageable, filter);
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

}