package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.model.ComputerEntry;
import com.sysadminanywhere.inventory.model.SoftwareEntity;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InventoryService {

    @Value("${ldap.host.username:}")
    String userName;

    @Value("${ldap.host.password:}")
    String password;

    ResolveService<ComputerEntry> resolveService = new ResolveService<>(ComputerEntry.class);

    private final LdapService ldapService;
    private final WmiService wmiService;

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final InstallationRepository installationRepository;

    public InventoryService(LdapService ldapService, WmiService wmiService, ComputerRepository computerRepository, SoftwareRepository softwareRepository, InstallationRepository installationRepository) {
        this.ldapService = ldapService;
        this.wmiService = wmiService;
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
    @Scheduled(cron = "${cron.expression}")
    public void scan() {

        log.info("Scan started");

        Boolean result = ldapService.login(userName, password);

        if (!result) {
            log.error("Unknown user: {}", userName);
            return;
        }

        wmiService.init(userName, password);

        List<ComputerEntry> computers = getComputers();

        log.info("Found {} computers", computers.size());

        for (ComputerEntry computerEntry : computers) {
            if (!computerEntry.isDisabled()) {
                Computer computer = checkComputer(computerEntry);
                List<SoftwareEntity> software = getSoftware(computerEntry.getCn());
                log.info("On computer {} found {} applications", computer.getName(), software.size());
                for (SoftwareEntity softwareEntity : software) {
                    checkSoftware(computer, softwareEntity);
                }

                checkForDeletedSoftware(computer, software);
            }
        }

        log.info("Scan stopped");

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
        List<Software> software = softwareRepository.findByNameAndVendorAndVersion(softwareEntity.getName(), softwareEntity.getVendor(), softwareEntity.getVersion());
        if (software.isEmpty()) {
            Software soft = new Software();
            soft.setName(softwareEntity.getName());
            soft.setVersion(softwareEntity.getVersion());
            soft.setVendor(softwareEntity.getVendor());

            return softwareRepository.save(soft);
        } else {
            return software.get(0);
        }
    }

    private List<SoftwareEntity> getSoftware(String hostName) {
        try {
            WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
            return wmiResolveService.GetValues(wmiService.execute(hostName, "Select * From Win32_Product"));
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    private List<ComputerEntry> getComputers() {
        List<Entry> result = ldapService.search("(objectClass=computer)");
        return resolveService.getADList(result);
    }

    private LocalDateTime getLocalDateTime(String installDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ld = LocalDate.parse(installDate, dateTimeFormatter);
        return ld.atStartOfDay();
    }

}