package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.LdapServiceClient;
import com.sysadminanywhere.domain.DirectorySetting;
import com.sysadminanywhere.model.ComputerItem;
import com.sysadminanywhere.model.SoftwareCount;
import com.sysadminanywhere.model.SoftwareOnComputer;
import com.sysadminanywhere.entity.Computer;
import com.sysadminanywhere.entity.Installation;
import com.sysadminanywhere.entity.Software;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.model.wmi.HardwareEntity;
import com.sysadminanywhere.model.wmi.SoftwareEntity;
import com.sysadminanywhere.repository.ComputerRepository;
import com.sysadminanywhere.repository.InstallationRepository;
import com.sysadminanywhere.repository.SoftwareRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class InventoryService {

    @Value("${ldap.host.username:}")
    String userName;

    @Value("${ldap.host.password:}")
    String password;

    @Value("${inventory.enabled:false}")
    boolean isEnabled;

    ResolveService<ComputerEntry> resolveService = new ResolveService<>(ComputerEntry.class);

    @Autowired
    private LdapServiceClient ldapServiceClient;

    private final ComputersService computersService;

    private LdapService ldapService;
    private WmiService wmiService;

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final InstallationRepository installationRepository;

    public InventoryService(ComputersService computersService, ComputerRepository computerRepository, SoftwareRepository softwareRepository, InstallationRepository installationRepository) {
        this.computersService = computersService;
        this.computerRepository = computerRepository;
        this.softwareRepository = softwareRepository;
        this.installationRepository = installationRepository;
    }

    /*

     ┌───────────── second (0-59)
     │ ┌───────────── minute (0 - 59)
     │ │ ┌───────────── hour (0 - 23)
     │ │ │ ┌───────────── day of the month (1 - 31)
     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
     │ │ │ │ │ ┌───────────── day of the week (0 - 7)
     │ │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
     │ │ │ │ │ │
     * * * * * *

    "0 0 12 * * *" every day at 12:00

    */

    @Transactional
    @Scheduled(cron = "${inventory.cron.expression}")
    public void scan() {

        if(!isEnabled)
            return;

        log.info("Scan started");

        ldapService = new LdapService(ldapServiceClient);
        wmiService = new WmiService();

        Boolean result = ldapService.login(userName, password);

        if (!result) {
            log.error("Unknown user: {}", userName);
            return;
        }

        wmiService.init(ldapService.getDomainName() + "\\" + userName, password);

        List<ComputerEntry> computers = getComputers();

        log.info("Found {} computers", computers.size());

        for (ComputerEntry computerEntry : computers) {
            if (!computerEntry.isDisabled()) {
                Computer computer = checkComputer(computerEntry);
                scanSoftware(computer);
                scanHardware(computer);
            }
        }

        ldapService = null;
        wmiService = null;

        log.info("Scan stopped");

    }

    private void scanSoftware(Computer computer) {
        List<SoftwareEntity> software = getSoftware(computer.getName());
        log.info("On computer {} found {} applications", computer.getName(), software.size());
        for (SoftwareEntity softwareEntity : software) {
            checkSoftware(computer, softwareEntity);
        }

        checkForDeletedSoftware(computer, software);
    }

    private void scanHardware(Computer computer) {
        List<HardwareEntity> hardware = getHardware(computer.getName());
        for (HardwareEntity hardwareEntity : hardware) {
            checkHardware(computer, hardwareEntity);
        }

        checkForDeletedHardware(computer, hardware);
    }

    private void checkHardware(Computer computer, HardwareEntity hardwareEntity) {

    }

    private void checkForDeletedHardware(Computer computer, List<HardwareEntity> hardware) {

    }

    @Transactional
    public void checkSoftware(Computer computer, SoftwareEntity softwareEntity) {
        Software software = checkSoftware(softwareEntity);

        List<Installation> installs = installationRepository.findAllByComputerAndSoftware(computer, software);

        if (installs.isEmpty()) {
            Installation installation = new Installation();
            installation.setComputer(computer);
            installation.setSoftware(software);
            installation.setCheckingDate(LocalDateTime.now());
            installation.setInstallDate(getLocalDateTime(softwareEntity.getInstallDate()));

            installationRepository.save(installation);
        } else {
            installs.get(0).setCheckingDate(LocalDateTime.now());
            installationRepository.save(installs.get(0));
        }
    }

    @Transactional
    public void checkForDeletedSoftware(Computer computer, List<SoftwareEntity> software) {
        List<Installation> installs = installationRepository.findAllByComputer(computer);

        for (Installation installation : installs) {
            List<SoftwareEntity> list = software.stream().filter(c ->
                    c.getName().equalsIgnoreCase(installation.getSoftware().getName())
                            && c.getVendor().equalsIgnoreCase(installation.getSoftware().getVendor())
                            && c.getVersion().equalsIgnoreCase(installation.getSoftware().getVersion())
            ).toList();

            if (list.isEmpty()) {
                log.info("Software {} not found on computer {}", installation.getSoftware(), computer.getName());
                installationRepository.delete(installation);
            }
        }
    }

    @Transactional
    public Computer checkComputer(ComputerEntry computerEntry) {
        List<Computer> computers = computerRepository.findAllByName(computerEntry.getCn());
        if (computers.isEmpty()) {
            Computer computer = new Computer();
            computer.setName(computerEntry.getCn());
            computer.setDns(computerEntry.getDnsHostName());
            return computerRepository.save(computer);
        } else {
            return computers.get(0);
        }
    }

    @Transactional
    public Software checkSoftware(SoftwareEntity softwareEntity) {
        List<Software> software = softwareRepository.findByNameAndVendor(softwareEntity.getName(), softwareEntity.getVendor());
        if (software.isEmpty()) {
            Software soft = new Software();
            soft.setName(softwareEntity.getName());
            soft.setVersion(softwareEntity.getVersion());
            soft.setVendor(softwareEntity.getVendor());

            return softwareRepository.save(soft);
        } else {
            Software soft = software.get(0);
            if (!softwareEntity.getVersion().equalsIgnoreCase(soft.getVersion())) {
                soft.setVersion(softwareEntity.getVersion());
                return softwareRepository.save(soft);
            } else {
                return soft;
            }
        }
    }

    private List<SoftwareEntity> getSoftware(String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.getValues(wmiService.execute(hostName, "Select * From Win32_Product"));
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ComputerEntry> getComputers() {
        return computersService.getAll("");
    }

    private LocalDateTime getLocalDateTime(String installDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ld = LocalDate.parse(installDate, dateTimeFormatter);
        return ld.atStartOfDay();
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable, Map<String, String> filters) {
        return softwareRepository.getSoftwareOnComputer(computerId, pageable);
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name") + "%";
        String vendor = filters.get("vendor") + "%";
        return softwareRepository.getSoftwareInstallationCount(name, vendor, pageable);
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        String name = filters.get("name") + "%";
        return computerRepository.getComputersWithSoftware(softwareId, name, pageable);
    }

    private List<HardwareEntity> getHardware(String hostName) {
        return new ArrayList<>();
    }

}