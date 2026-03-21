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
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.Hardware;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import com.sysadminanywhere.inventory.repository.HardwareRepository;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import com.sysadminanywhere.inventory.wmi.*;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ComputerHardwareRepository computerHardwareRepository;
    private final HardwareRepository hardwareRepository;

    public InventoryService(AuthService authService,
                            ComputersServiceClient computersServiceClient,
                            WmiServiceClient wmiServiceClient,
                            ComputerRepository computerRepository,
                            SoftwareRepository softwareRepository,
                            InstallationRepository installationRepository,
                            ComputerHardwareRepository computerHardwareRepository,
                            HardwareRepository hardwareRepository) {

        this.authService = authService;
        this.computersServiceClient = computersServiceClient;
        this.wmiServiceClient = wmiServiceClient;
        this.computerRepository = computerRepository;
        this.softwareRepository = softwareRepository;
        this.installationRepository = installationRepository;
        this.computerHardwareRepository = computerHardwareRepository;
        this.hardwareRepository = hardwareRepository;
    }

    /*
     ┌───────────── second (0-59)
     │ ┌───────────── minute (0 - 59)
     │ │ ┌───────────── hour (0 - 23)
     │ │ │ ┌───────────── day of month (1 - 31)
     │ │ │ │ ┌───────────── month (1 - 12) (or JAN-DEC)
     │ │ │ │ │ ┌───────────── day of week (0 - 7)
     │ │ │ │ │          (0 or 7 is Sunday, or MON-SUN)
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
                    scanHardware(computerEntry.getCn());
                } catch (Exception ex) {
                    log.error("Error scanning on computer {}: {}",
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
    @Transactional
    public void scanHardware(String hostName) {
        if (hostName == null || hostName.isEmpty()) {
            log.error("Host name is null or empty for hardware scan");
            return;
        }

        log.info("Scanning hardware on computer {}", hostName);

        List<Object> hardwareItems = new ArrayList<>();

        try {
            // Get Computer System
            var computerSystemResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_ComputerSystem"));
            if (computerSystemResponse != null && computerSystemResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> computerSystemData = (List<Map<String, Object>>) computerSystemResponse.getBody();
                if (computerSystemData != null && !computerSystemData.isEmpty()) {
                    WmiResolveService<ComputerSystemEntity> wmiResolveService = new WmiResolveService<>(ComputerSystemEntity.class);
                    List<ComputerSystemEntity> computerSystems = wmiResolveService.getValues(computerSystemData);
                    hardwareItems.addAll(computerSystems);
                }
            }

            // Get BIOS
            var biosResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_BIOS"));
            if (biosResponse != null && biosResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> biosData = (List<Map<String, Object>>) biosResponse.getBody();
                if (biosData != null && !biosData.isEmpty()) {
                    WmiResolveService<BIOSEntity> wmiResolveService = new WmiResolveService<>(BIOSEntity.class);
                    List<BIOSEntity> biosList = wmiResolveService.getValues(biosData);
                    hardwareItems.addAll(biosList);
                }
            }

            // Get BaseBoard
            var baseboardResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_BaseBoard"));
            if (baseboardResponse != null && baseboardResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> baseboardData = (List<Map<String, Object>>) baseboardResponse.getBody();
                if (baseboardData != null && !baseboardData.isEmpty()) {
                    WmiResolveService<BaseboardEntity> wmiResolveService = new WmiResolveService<>(BaseboardEntity.class);
                    List<BaseboardEntity> baseboards = wmiResolveService.getValues(baseboardData);
                    hardwareItems.addAll(baseboards);
                }
            }

            // Get Processor
            var processorResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_Processor"));
            if (processorResponse != null && processorResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> processorData = (List<Map<String, Object>>) processorResponse.getBody();
                if (processorData != null && !processorData.isEmpty()) {
                    WmiResolveService<ProcessorEntity> wmiResolveService = new WmiResolveService<>(ProcessorEntity.class);
                    List<ProcessorEntity> processors = wmiResolveService.getValues(processorData);
                    hardwareItems.addAll(processors);
                }
            }

            // Get Physical Memory
            var memoryResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_PhysicalMemory"));
            if (memoryResponse != null && memoryResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> memoryData = (List<Map<String, Object>>) memoryResponse.getBody();
                if (memoryData != null && !memoryData.isEmpty()) {
                    WmiResolveService<PhysicalMemoryEntity> wmiResolveService = new WmiResolveService<>(PhysicalMemoryEntity.class);
                    List<PhysicalMemoryEntity> memoryModules = wmiResolveService.getValues(memoryData);
                    hardwareItems.addAll(memoryModules);
                }
            }

            // Get Disk Drive
            var diskDriveResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_DiskDrive"));
            if (diskDriveResponse != null && diskDriveResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> diskDriveData = (List<Map<String, Object>>) diskDriveResponse.getBody();
                if (diskDriveData != null && !diskDriveData.isEmpty()) {
                    WmiResolveService<DiskDriveEntity> wmiResolveService = new WmiResolveService<>(DiskDriveEntity.class);
                    List<DiskDriveEntity> diskDrives = wmiResolveService.getValues(diskDriveData);
                    hardwareItems.addAll(diskDrives);
                }
            }

            // Get Video Controller
            var videoControllerResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_VideoController"));
            if (videoControllerResponse != null && videoControllerResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> videoControllerData = (List<Map<String, Object>>) videoControllerResponse.getBody();
                if (videoControllerData != null && !videoControllerData.isEmpty()) {
                    WmiResolveService<VideoControllerEntity> wmiResolveService = new WmiResolveService<>(VideoControllerEntity.class);
                    List<VideoControllerEntity> videoControllers = wmiResolveService.getValues(videoControllerData);
                    hardwareItems.addAll(videoControllers);
                }
            }

            // Get Logical Disk
            var logicalDiskResponse = wmiServiceClient.execute(new ExecuteDto(hostName, "SELECT * FROM Win32_LogicalDisk"));
            if (logicalDiskResponse != null && logicalDiskResponse.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> logicalDiskData = (List<Map<String, Object>>) logicalDiskResponse.getBody();
                if (logicalDiskData != null && !logicalDiskData.isEmpty()) {
                    WmiResolveService<LogicalDiskEntity> wmiResolveService = new WmiResolveService<>(LogicalDiskEntity.class);
                    List<LogicalDiskEntity> logicalDisks = wmiResolveService.getValues(logicalDiskData);
                    hardwareItems.addAll(logicalDisks);
                }
            }

        } catch (Exception ex) {
            log.error("Failed to scan hardware on computer {}: {}", hostName, ex.getMessage(), ex);
        }

        if (!hardwareItems.isEmpty()) {
            Computer computer = checkComputer(hostName);
            if (computer != null) {
                log.info("On computer {} found {} hardware items", computer.getName(), hardwareItems.size());

                for (Object hardwareItem : hardwareItems) {
                    if (hardwareItem != null) {
                        try {
                            processHardwareItem(computer, hardwareItem);
                        } catch (Exception ex) {
                            log.error("Error checking hardware {} on computer {}: {}",
                                    getHardwareType(hardwareItem), hostName, ex.getMessage());
                        }
                    }
                }

                checkForDeletedHardware(computer, hardwareItems);
            }
        }
    }

    private String getHardwareType(Object hardwareItem) {
        if (hardwareItem == null) return "Unknown";
        
        String className = hardwareItem.getClass().getSimpleName();
        switch (className) {
            case "ComputerSystemEntity": return "ComputerSystem";
            case "BIOSEntity": return "BIOS";
            case "BaseboardEntity": return "BaseBoard";
            case "ProcessorEntity": return "Processor";
            case "PhysicalMemoryEntity": return "PhysicalMemory";
            case "DiskDriveEntity": return "DiskDrive";
            case "VideoControllerEntity": return "VideoController";
            case "LogicalDiskEntity": return "LogicalDisk";
            default: return className;
        }
    }

    private String getHardwareName(Object hardwareItem) {
        if (hardwareItem == null) return "Unknown";
        
        try {
            String className = hardwareItem.getClass().getSimpleName();
            
            // Handle specific entity types with their relevant fields
            switch (className) {
                case "BIOSEntity":
                    return getBiosName((BIOSEntity) hardwareItem);
                case "BaseboardEntity":
                    return getBaseboardName((BaseboardEntity) hardwareItem);
                case "ProcessorEntity":
                    return getProcessorName((ProcessorEntity) hardwareItem);
                case "PhysicalMemoryEntity":
                    return getMemoryName((PhysicalMemoryEntity) hardwareItem);
                case "DiskDriveEntity":
                    return getDiskDriveName((DiskDriveEntity) hardwareItem);
                case "VideoControllerEntity":
                    return getVideoControllerName((VideoControllerEntity) hardwareItem);
                case "LogicalDiskEntity":
                    return getLogicalDiskName((LogicalDiskEntity) hardwareItem);
                case "ComputerSystemEntity":
                    return getComputerSystemName((ComputerSystemEntity) hardwareItem);
                default:
                    return getGenericHardwareName(hardwareItem);
            }
        } catch (Exception ex) {
            log.warn("Failed to get hardware name for {}: {}", hardwareItem.getClass().getSimpleName(), ex.getMessage());
            return hardwareItem.getClass().getSimpleName();
        }
    }
    
    private String getBiosName(BIOSEntity bios) {
        if (bios.getManufacturer() != null && !bios.getManufacturer().isEmpty()) {
            return bios.getManufacturer() + (bios.getVersion() != null ? " " + bios.getVersion() : "");
        }
        return "BIOS";
    }
    
    private String getBaseboardName(BaseboardEntity baseboard) {
        if (baseboard.getManufacturer() != null && !baseboard.getManufacturer().isEmpty()) {
            return baseboard.getManufacturer() + (baseboard.getProduct() != null ? " " + baseboard.getProduct() : "");
        }
        return "BaseBoard";
    }
    
    private String getProcessorName(ProcessorEntity processor) {
        if (processor.getName() != null && !processor.getName().isEmpty()) {
            return processor.getName();
        }
        if (processor.getManufacturer() != null && !processor.getManufacturer().isEmpty()) {
            return processor.getManufacturer() + (processor.getDescription() != null ? " " + processor.getDescription() : "");
        }
        return "Processor";
    }
    
    private String getMemoryName(PhysicalMemoryEntity memory) {
        if (memory.getManufacturer() != null && !memory.getManufacturer().isEmpty()) {
            return memory.getManufacturer() + " " + (memory.getCapacity() > 0 ? memory.getCapacity() : "") + "MB";
        }
        if (memory.getCapacity() > 0) {
            return "Memory " + memory.getCapacity() + "MB";
        }
        return "Physical Memory";
    }
    
    private String getDiskDriveName(DiskDriveEntity disk) {
        if (disk.getModel() != null && !disk.getModel().isEmpty()) {
            return disk.getModel();
        }
        if (disk.getManufacturer() != null && !disk.getManufacturer().isEmpty()) {
            return disk.getManufacturer() + " Disk";
        }
        return "Disk Drive";
    }
    
    private String getVideoControllerName(VideoControllerEntity video) {
        if (video.getName() != null && !video.getName().isEmpty()) {
            return video.getName();
        }
        if (video.getAdapterRAM() > 0) {
            return "Video Controller " + video.getAdapterRAM();
        }
        return "Video Controller";
    }
    
    private String getLogicalDiskName(LogicalDiskEntity disk) {
        if (disk.getName() != null && !disk.getName().isEmpty()) {
            return disk.getName() + (disk.getDescription() != null ? " (" + disk.getDescription() + ")" : "");
        }
        return "Logical Disk";
    }
    
    private String getComputerSystemName(ComputerSystemEntity system) {
        if (system.getManufacturer() != null && !system.getManufacturer().isEmpty()) {
            return system.getManufacturer() + " " + (system.getModel() != null ? system.getModel() : "");
        }
        return "Computer System";
    }
    
    private String getGenericHardwareName(Object hardwareItem) {
        try {
            // Fallback to reflection for unknown types
            java.lang.reflect.Field[] fields = hardwareItem.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();
                
                if (fieldName.contains("name") || fieldName.contains("caption") || fieldName.contains("description") 
                        || fieldName.contains("manufacturer") || fieldName.contains("model") || fieldName.contains("product")) {
                    Object value = field.get(hardwareItem);
                    if (value != null && !value.toString().isEmpty()) {
                        return value.toString();
                    }
                }
            }
            
            return hardwareItem.getClass().getSimpleName();
        } catch (Exception ex) {
            return "Unknown Hardware";
        }
    }

    @Transactional
    public void processHardwareItem(Computer computer, Object hardwareItem) {
        String hardwareType = getHardwareType(hardwareItem);
        String hardwareName = getHardwareName(hardwareItem);
        
        // Create or find hardware record
        Hardware hardware = checkHardware(hardwareType, hardwareName);
        if (hardware == null) {
            log.error("Failed to get or create hardware record for {} on computer {}",
                    hardwareType, computer.getName());
            return;
        }

        try {
            List<ComputerHardware> existingLinks = computerHardwareRepository.findAllByComputerAndHardware(computer, hardware);
            if (existingLinks == null || existingLinks.isEmpty()) {
                ComputerHardware computerHardware = new ComputerHardware();
                computerHardware.setComputer(computer);
                computerHardware.setHardware(hardware);
                computerHardware.setCheckingDate(LocalDateTime.now());
                computerHardwareRepository.save(computerHardware);
                log.debug("Created new computer hardware record for {} on {}",
                        hardwareType, computer.getName());
            } else {
                ComputerHardware existingLink = existingLinks.get(0);
                existingLink.setCheckingDate(LocalDateTime.now());
                computerHardwareRepository.save(existingLink);
                log.debug("Updated existing computer hardware record for {} on {}",
                        hardwareType, computer.getName());
            }
        } catch (Exception ex) {
            log.error("Error saving computer hardware for {} on computer {}: {}",
                    hardwareType, computer.getName(), ex.getMessage(), ex);
        }
    }

    @Transactional
    public Hardware checkHardware(String type, String name) {
        if (type == null || type.isEmpty()) {
            log.error("Hardware type is null or empty in checkHardware");
            return null;
        }

        if (name == null || name.isEmpty()) {
            log.error("Hardware name is null or empty in checkHardware");
            return null;
        }

        try {
            List<Hardware> hardwareList = hardwareRepository.findByNameAndType(name, type);

            if (hardwareList == null || hardwareList.isEmpty()) {
                Hardware hw = new Hardware();
                hw.setName(name);
                hw.setType(type);
                Hardware savedHardware = hardwareRepository.save(hw);
                log.debug("Created new hardware record: {} - {}", name, type);
                return savedHardware;
            } else {
                Hardware hw = hardwareList.get(0);
                log.debug("Found existing hardware record for {} - {}", name, type);
                return hw;
            }
        } catch (Exception ex) {
            log.error("Error checking hardware {}: {}", name, ex.getMessage(), ex);
            return null;
        }
    }

    @Transactional
    public void checkForDeletedHardware(Computer computer, List<Object> hardwareItems) {
        if (computer == null) {
            log.error("Computer is null in checkForDeletedHardware");
            return;
        }

        if (hardwareItems == null) {
            log.warn("Hardware list is null in checkForDeletedHardware for computer {}", computer.getName());
            hardwareItems = Collections.emptyList();
        }

        log.debug("Checking for deleted hardware on computer {}", computer.getName());

        List<ComputerHardware> existingLinks;
        try {
            existingLinks = computerHardwareRepository.findAllByComputerWithHardware(computer);
        } catch (Exception ex) {
            log.error("Error finding computer hardware for computer {}: {}",
                    computer.getName(), ex.getMessage());
            return;
        }

        if (existingLinks == null || existingLinks.isEmpty()) {
            return;
        }

        // Create a set of current hardware identifiers for comparison
        Set<String> currentHardwareIdentifiers = hardwareItems.stream()
                .filter(h -> h != null)
                .map(h -> getHardwareType(h) + "|" + getHardwareName(h))
                .collect(Collectors.toSet());

        for (ComputerHardware computerHardware : existingLinks) {
            if (computerHardware == null || computerHardware.getHardware() == null) {
                continue;
            }

            try {
                String existingIdentifier = computerHardware.getHardware().getType() + "|" + computerHardware.getHardware().getName();
                if (!currentHardwareIdentifiers.contains(existingIdentifier)) {
                    log.info("Hardware {} not found on computer {}, removing record",
                            computerHardware.getHardware().getName(), computer.getName());
                    computerHardwareRepository.delete(computerHardware);
                }
            } catch (Exception ex) {
                log.error("Error processing computer hardware for computer {}: {}",
                        computer.getName(), ex.getMessage());
            }
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
                installation.setCheckingDate(LocalDateTime.now());

                LocalDateTime installDate = getLocalDateTime(softwareEntity.getInstallDate());
                installation.setInstallDate(installDate);

                installationRepository.save(installation);
                log.debug("Created new installation record for {} on {}",
                        softwareEntity.getName(), computer.getName());
            } else {
                Installation existingInstallation = installs.get(0);
                existingInstallation.setCheckingDate(LocalDateTime.now());
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
}
