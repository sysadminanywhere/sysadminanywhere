package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.wmi.SoftwareEntity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class SoftwareService {

    private final WmiServiceClient wmiServiceClient;

    private final SoftwareRepository softwareRepository;
    private final InstallationRepository installationRepository;


    public SoftwareService(WmiServiceClient wmiServiceClient,
                           SoftwareRepository softwareRepository,
                           InstallationRepository installationRepository) {

        this.wmiServiceClient = wmiServiceClient;
        this.softwareRepository = softwareRepository;
        this.installationRepository = installationRepository;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void scanSoftware(Computer computer) {
        String hostName = computer.getName();

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
                    computer.getName(), softwareEntity.getName(), ex.getMessage());
            return;
        }

        try {
            if (installs == null || installs.isEmpty()) {
                Installation installation = new Installation();
                installation.setComputer(computer);
                installation.setSoftware(software);

                LocalDateTime installDate = getLocalDateTime(softwareEntity.getInstallDate());
                installation.setInstallDate(installDate);

                installationRepository.save(installation);
                log.debug("Created new installation record for {} on {}",
                        softwareEntity.getName(), computer.getName());
            } else {
                Installation existingInstallation = installs.get(0);
                installationRepository.save(existingInstallation);
                log.debug("Updated existing installation record for {} on {}",
                        softwareEntity.getName(), computer.getName());
            }
        } catch (Exception ex) {
            log.error("Error saving installation for computer {} and software {}: {}",
                    computer.getName(), softwareEntity.getName(), ex.getMessage(), ex);
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


}
