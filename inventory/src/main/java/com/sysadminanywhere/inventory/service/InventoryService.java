package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.inventory.model.ComputerItem;
import com.sysadminanywhere.common.inventory.model.SoftwareCount;
import com.sysadminanywhere.common.inventory.model.SoftwareOnComputer;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.ComputersServiceClient;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.wmi.HardwareEntity;
import com.sysadminanywhere.inventory.wmi.SoftwareEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class InventoryService {

    @Value("${ldap.host.username}")
    private String username;

    @Value("${ldap.host.password}")
    private String password;

    private final AuthService authService;
    private final ComputersServiceClient computersServiceClient;
    private final WmiServiceClient wmiServiceClient;

    private final ComputerRepository computerRepository;
    private final SoftwareRepository softwareRepository;
    private final InstallationRepository installationRepository;

    public InventoryService(AuthService authService,
                            ComputersServiceClient computersServiceClient,
                            WmiServiceClient wmiServiceClient,
                            ComputerRepository computerRepository,
                            SoftwareRepository softwareRepository,
                            InstallationRepository installationRepository) {

        this.authService = authService;
        this.computersServiceClient = computersServiceClient;
        this.wmiServiceClient = wmiServiceClient;
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

    @SneakyThrows
    @Scheduled(cron = "${cron.expression}")
    public void scan() {
        log.info("Scan started");

        log.info("Logging in to directory service");
        boolean authenticated = authenticate();
        if (!authenticated) {
            log.error("Failed to authenticate with directory service. Aborting scan.");
            return;
        }

        log.info("Requesting computers from directory service");
        List<ComputerEntry> computers = getComputers();

        if (computers == null) {
            log.error("Received null computers list from directory service. Aborting scan.");
            return;
        }

        log.info("Found {} computers", computers.size());

        for (ComputerEntry computerEntry : computers) {
            if (computerEntry != null && !computerEntry.isDisabled()) {
                try {
                    scanSoftware(computerEntry.getCn());
                } catch (Exception ex) {
                    log.error("Error scanning software on computer {}: {}",
                            computerEntry.getCn(), ex.getMessage(), ex);
                }
            }
        }

        log.info("Scan stopped");
    }

    private boolean authenticate() {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            log.error("Username or password for directory service is not set. Aborting scan.");
            return false;
        }

        try {
            JwtResponse jwtResponse = authService.authenticate(username, password);

            if (jwtResponse == null || jwtResponse.token() == null) {
                log.error("Received null JWT response or token");
                return false;
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());
            auth.setDetails(jwtResponse.token());
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("Successfully authenticated with directory service");
            return true;
        } catch (Exception ex) {
            log.error("Failed to authenticate with directory service: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void scanSoftware(String hostName) {
        if (hostName == null || hostName.isEmpty()) {
            log.error("Host name is null or empty for software scan");
            return;
        }

        log.info("Scanning software on computer {}", hostName);

        String query = "Select Name, Vendor, Version From Win32_Product";
        List<Map<String, Object>> list;

        try {
            var response = wmiServiceClient.execute(new ExecuteDto(hostName, query));

            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to execute WMI query on computer {}: HTTP {}",
                        hostName, response != null ? response.getStatusCode() : "NULL");
                return;
            }

            list = (List<Map<String, Object>>) response.getBody();
        } catch (Exception ex) {
            log.error("Failed to execute WMI query on computer {}: {}", hostName, ex.getMessage());
            return;
        }

        if (list == null) {
            log.error("WMI client returned null for host {}", hostName);
            return;
        }

        WmiResolveService<SoftwareEntity> wmiResolveService = new WmiResolveService<>(SoftwareEntity.class);
        List<SoftwareEntity> software;

        try {
            software = wmiResolveService.getValues(list);
        } catch (Exception ex) {
            log.error("Failed to resolve WMI values for host {}: {}", hostName, ex.getMessage());
            return;
        }

        if (software == null) {
            log.warn("No software data resolved for computer {}", hostName);
            return;
        }

        Computer computer = checkComputer(hostName);
        if (computer == null) {
            log.error("Failed to get or create computer record for {}", hostName);
            return;
        }

        log.info("On computer {} found {} applications", computer.getName(), software.size());

        for (SoftwareEntity softwareEntity : software) {
            if (softwareEntity != null) {
                try {
                    checkSoftware(computer, softwareEntity);
                } catch (Exception ex) {
                    log.error("Error checking software {} on computer {}: {}",
                            softwareEntity.getName(), hostName, ex.getMessage());
                }
            }
        }

        checkForDeletedSoftware(computer, software);
    }

    private void scanHardware(Computer computer) {
        if (computer == null || computer.getName() == null) {
            log.error("Cannot scan hardware: computer or computer name is null");
            return;
        }

        log.info("Scanning hardware on computer {}", computer.getName());

        List<HardwareEntity> hardware = getHardware(computer.getName());

        if (hardware == null) {
            log.warn("No hardware data received for computer {}", computer.getName());
            return;
        }

        for (HardwareEntity hardwareEntity : hardware) {
            if (hardwareEntity != null) {
                try {
                    checkHardware(computer, hardwareEntity);
                } catch (Exception ex) {
                    log.error("Error checking hardware on computer {}: {}",
                            computer.getName(), ex.getMessage());
                }
            }
        }

        checkForDeletedHardware(computer, hardware);
    }

    private void checkHardware(Computer computer, HardwareEntity hardwareEntity) {
        // TODO: Implement hardware checking logic
        log.debug("Checking hardware for computer {}: {}", computer.getName(), hardwareEntity);
    }

    private void checkForDeletedHardware(Computer computer, List<HardwareEntity> hardware) {
        // TODO: Implement hardware deletion check logic
        log.debug("Checking for deleted hardware on computer {}", computer.getName());
    }

    @Transactional
    public void checkSoftware(Computer computer, SoftwareEntity softwareEntity) {
        // Validate input parameters
        if (computer == null) {
            log.error("Computer is null in checkSoftware");
            return;
        }

        if (softwareEntity == null) {
            log.error("SoftwareEntity is null in checkSoftware for computer {}", computer.getName());
            return;
        }

        log.debug("Checking software {} for computer {}", softwareEntity.getName(), computer.getName());

        Software software = checkSoftware(softwareEntity);

        if (software == null) {
            log.error("Failed to get or create software record for {} on computer {}",
                    softwareEntity.getName(), computer.getName());
            return;
        }

        List<Installation> installs;
        try {
            installs = installationRepository.findAllByComputerAndSoftware(computer, software);
        } catch (Exception ex) {
            log.error("Error finding installations for computer {} and software {}: {}",
                    computer.getName(), software.getName(), ex.getMessage());
            return;
        }

        try {
            if (installs == null || installs.isEmpty()) {
                Installation installation = new Installation();
                installation.setComputer(computer);
                installation.setSoftware(software);
                installation.setCheckingDate(LocalDateTime.now());

                LocalDateTime installDate = getLocalDateTime(softwareEntity.getInstallDate());
                installation.setInstallDate(installDate);

                installationRepository.save(installation);
                log.debug("Created new installation record for {} on {}",
                        software.getName(), computer.getName());
            } else {
                Installation existingInstallation = installs.get(0);
                existingInstallation.setCheckingDate(LocalDateTime.now());
                installationRepository.save(existingInstallation);
                log.debug("Updated existing installation record for {} on {}",
                        software.getName(), computer.getName());
            }
        } catch (Exception ex) {
            log.error("Error saving installation for computer {} and software {}: {}",
                    computer.getName(), software.getName(), ex.getMessage(), ex);
        }
    }

    @Transactional
    public void checkForDeletedSoftware(Computer computer, List<SoftwareEntity> software) {
        if (computer == null) {
            log.error("Computer is null in checkForDeletedSoftware");
            return;
        }

        if (software == null) {
            log.warn("Software list is null in checkForDeletedSoftware for computer {}", computer.getName());
            software = Collections.emptyList();
        }

        log.debug("Checking for deleted software on computer {}", computer.getName());

        List<Installation> installs;
        try {
            installs = installationRepository.findAllByComputer(computer);
        } catch (Exception ex) {
            log.error("Error finding installations for computer {}: {}",
                    computer.getName(), ex.getMessage());
            return;
        }

        if (installs == null || installs.isEmpty()) {
            return;
        }

        for (Installation installation : installs) {
            if (installation == null || installation.getSoftware() == null) {
                continue;
            }

            try {
                List<SoftwareEntity> list = software.stream()
                        .filter(c -> c != null)
                        .filter(c -> Objects.equals(c.getName(), installation.getSoftware().getName())
                                && Objects.equals(c.getVendor(), installation.getSoftware().getVendor())
                                && Objects.equals(c.getVersion(), installation.getSoftware().getVersion()))
                        .toList();

                if (list.isEmpty()) {
                    log.info("Software {} not found on computer {}",
                            installation.getSoftware().getName(), computer.getName());
                    installationRepository.delete(installation);
                }
            } catch (Exception ex) {
                log.error("Error processing installation for software on computer {}: {}",
                        computer.getName(), ex.getMessage());
            }
        }
    }

    public Computer checkComputer(String hostName) {
        if (hostName == null || hostName.isEmpty()) {
            log.error("Host name is null or empty in checkComputer");
            return null;
        }

        try {
            List<Computer> computers = computerRepository.findAllByName(hostName);

            if (computers == null || computers.isEmpty()) {
                Computer computer = new Computer();
                computer.setName(hostName);
                Computer savedComputer = computerRepository.save(computer);
                log.debug("Created new computer record for {}", hostName);
                return savedComputer;
            } else {
                log.debug("Found existing computer record for {}", hostName);
                return computers.get(0);
            }
        } catch (Exception ex) {
            log.error("Error checking computer {}: {}", hostName, ex.getMessage(), ex);
            return null;
        }
    }

    public Software checkSoftware(SoftwareEntity softwareEntity) {
        if (softwareEntity == null) {
            log.error("SoftwareEntity is null in checkSoftware");
            return null;
        }

        String name = softwareEntity.getName();
        String vendor = softwareEntity.getVendor();

        if (name == null) {
            log.error("Software name is null in SoftwareEntity");
            return null;
        }

        if (vendor == null) {
            vendor = "";
        }

        try {
            List<Software> softwareList = softwareRepository.findByNameAndVendor(name, vendor);

            if (softwareList == null || softwareList.isEmpty()) {
                Software soft = new Software();
                soft.setName(name);
                soft.setVersion(softwareEntity.getVersion());
                soft.setVendor(vendor);

                Software savedSoftware = softwareRepository.save(soft);
                log.debug("Created new software record: {} - {}", name, vendor);
                return savedSoftware;
            } else {
                Software soft = softwareList.get(0);

                // Безопасное сравнение версий
                String newVersion = softwareEntity.getVersion();
                String oldVersion = soft.getVersion();

                if (!areVersionsEqual(newVersion, oldVersion)) {
                    soft.setVersion(newVersion);
                    Software updatedSoftware = softwareRepository.save(soft);
                    log.debug("Updated software version for {}: {} -> {}",
                            name, oldVersion, newVersion);
                    return updatedSoftware;
                } else {
                    return soft;
                }
            }
        } catch (Exception ex) {
            log.error("Error checking software {}: {}", name, ex.getMessage(), ex);
            return null;
        }
    }

    private boolean areVersionsEqual(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return true;
        }
        if (v1 == null || v2 == null) {
            return false;
        }
        return v1.equalsIgnoreCase(v2);
    }

    @SneakyThrows
    private List<ComputerEntry> getComputers() {
        try {
            return computersServiceClient.getList("", "cn", "useraccountcontrol", "dnshostname");
        } catch (Exception ex) {
            log.error("Error getting computers from directory service: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    private LocalDateTime getLocalDateTime(String installDate) {
        if (installDate == null || installDate.isEmpty()) {
            log.debug("Install date is null or empty, using current date");
            return LocalDateTime.now();
        }

        try {
            // Некоторые WMI запросы могут возвращать дату в другом формате
            // Например: "20240312" или "20240312000000.000000+***"
            String cleanDate = installDate;
            if (installDate.length() >= 8) {
                cleanDate = installDate.substring(0, 8); // Берем первые 8 символов (yyyyMMdd)
            }

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate ld = LocalDate.parse(cleanDate, dateTimeFormatter);
            return ld.atStartOfDay();
        } catch (Exception ex) {
            log.error("Failed to parse install date '{}': {}, using current date", installDate, ex.getMessage());
            return LocalDateTime.now();
        }
    }

    public Page<SoftwareOnComputer> getSoftwareOnComputer(Long computerId, Pageable pageable, Map<String, String> filters) {
        if (computerId == null) {
            log.error("Computer ID is null in getSoftwareOnComputer");
            return Page.empty(pageable);
        }

        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        try {
            return softwareRepository.getSoftwareOnComputer(computerId, pageable);
        } catch (Exception ex) {
            log.error("Error getting software on computer {}: {}", computerId, ex.getMessage());
            return Page.empty(pageable);
        }
    }

    public Page<SoftwareCount> getSoftwareCount(Pageable pageable, Map<String, String> filters) {
        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        if (filters == null) {
            filters = new HashMap<>();
        }

        try {
            String name = filters.getOrDefault("name", "");
            if (name.isEmpty()) {
                name = "%";
            } else {
                name = name + "%";
            }

            String vendor = filters.getOrDefault("vendor", "");
            if (vendor.isEmpty()) {
                vendor = "%";
            } else {
                vendor = vendor + "%";
            }

            return softwareRepository.getSoftwareInstallationCount(name, vendor, pageable);
        } catch (Exception ex) {
            log.error("Error getting software count: {}", ex.getMessage());
            return Page.empty(pageable);
        }
    }

    public Page<ComputerItem> getComputersWithSoftware(Long softwareId, Pageable pageable, Map<String, String> filters) {
        if (softwareId == null) {
            log.error("Software ID is null in getComputersWithSoftware");
            return Page.empty(pageable);
        }

        if (pageable == null) {
            pageable = Pageable.unpaged();
        }

        if (filters == null) {
            filters = new HashMap<>();
        }

        try {
            String name = filters.get("name");
            if (name == null || name.isEmpty()) {
                name = "%";
            } else {
                name = name + "%";
            }

            return computerRepository.getComputersWithSoftware(softwareId, name, pageable);
        } catch (Exception ex) {
            log.error("Error getting computers with software {}: {}", softwareId, ex.getMessage());
            return Page.empty(pageable);
        }
    }

    private List<HardwareEntity> getHardware(String hostName) {
        // TODO: Implement hardware retrieval
        return new ArrayList<>();
    }

}