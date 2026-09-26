package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.model.HardwareType;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.HardwareModelRepository;
import com.sysadminanywhere.inventory.repository.HardwarePropertyRepository;
import com.sysadminanywhere.inventory.repository.HardwareValueRepository;
import com.sysadminanywhere.inventory.repository.HardwareChangeRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.sysadminanywhere.inventory.entity.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class HardwareService {

    private final WmiServiceClient wmiServiceClient;
    private final ComputerHardwareRepository computerHardwareRepository;
    private final HardwareModelRepository hardwareModelRepository;
    private final HardwarePropertyRepository hardwarePropertyRepository;
    private final HardwareValueRepository hardwareValueRepository;
    private final HardwareChangeRepository hardwareChangeRepository;

    @Value("${inventory.wmi.retry-attempts:3}")
    private int retryAttempts;
    @Value("${inventory.wmi.retry-delay-ms:500}")
    private long retryDelayMs;

    private final Map<String, HardwareModel> modelCache = new HashMap<>();

    public HardwareService(WmiServiceClient wmiServiceClient,
                           ComputerHardwareRepository computerHardwareRepository,
                           HardwareModelRepository hardwareModelRepository,
                           HardwarePropertyRepository hardwarePropertyRepository,
                           HardwareValueRepository hardwareValueRepository,
                           HardwareChangeRepository hardwareChangeRepository) {

        this.wmiServiceClient = wmiServiceClient;
        this.computerHardwareRepository = computerHardwareRepository;
        this.hardwareModelRepository = hardwareModelRepository;
        this.hardwarePropertyRepository = hardwarePropertyRepository;
        this.hardwareValueRepository = hardwareValueRepository;
        this.hardwareChangeRepository = hardwareChangeRepository;
    }
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transactional
    public void scanHardware(Computer computer) {
        String hostName = computer.getName();
        log.info("Scanning hardware on computer {}", hostName);
        boolean firstHistoryScan = !hardwareChangeRepository.existsByComputerIdAndChangeTypeNot(
                computer.getId(), "CONFIGURATION_CHANGED");

        List<Map<String, Object>> diskDrives = execute(hostName, "SELECT * FROM Win32_DiskDrive");
        List<Map<String, Object>> operatingSystems = execute(hostName, "SELECT * FROM Win32_OperatingSystem");
        List<Map<String, Object>> processors = execute(hostName, "SELECT * FROM Win32_Processor");
        List<Map<String, Object>> videoControllers = execute(hostName, "SELECT * FROM Win32_VideoController");
        List<Map<String, Object>> physicalMemory = execute(hostName, "SELECT * FROM Win32_PhysicalMemory");
        List<Map<String, Object>> baseBoards = execute(hostName, "SELECT * FROM Win32_BaseBoard");
        List<Map<String, Object>> bios = execute(hostName, "SELECT * FROM Win32_BIOS");
        List<Map<String, Object>> computerSystems = execute(hostName, "SELECT * FROM Win32_ComputerSystem");
        List<Map<String, Object>> patches = execute(hostName, "SELECT * FROM Win32_QuickFixEngineering");
        boolean completeScan = java.util.stream.Stream.of(diskDrives, operatingSystems, processors, videoControllers,
                physicalMemory, baseBoards, bios, computerSystems, patches).noneMatch(Objects::isNull);

        // Get current hardware models for this computer
        Set<Long> currentHardwareModelIds = new HashSet<>();
        Set<Long> processedHardwareModelIds = new HashSet<>();
        List<ComputerHardware> previousHardware = computerHardwareRepository.findByComputerId(computer.getId());
        Set<Long> matchedHardwareIds = new HashSet<>();
        
        saveHardware(computer, HardwareType.DISK_DRIVE, diskDrives, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.OPERATING_SYSTEM, operatingSystems, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.PROCESSOR, processors, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.VIDEO_CONTROLLER, videoControllers, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.PHYSICAL_MEMORY, physicalMemory, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.BASE_BOARD, baseBoards, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.BIOS, bios, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.COMPUTER_SYSTEM, computerSystems, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        saveHardware(computer, HardwareType.PATCH, patches, currentHardwareModelIds, processedHardwareModelIds, previousHardware, matchedHardwareIds, firstHistoryScan);
        
        // Remove hardware that no longer exists
        if (completeScan) {
            removeObsoleteHardware(computer, currentHardwareModelIds);
        } else {
            log.warn("Keeping previous hardware records for {} because one or more WMI queries failed", hostName);
        }
    }

    private List<Map<String, Object>> execute(String hostName, String query) {
        List<Map<String, Object>> list = null;

        for (int attempt = 1; attempt <= Math.max(1, retryAttempts); attempt++) {
          try {
            var response = wmiServiceClient.execute(new ExecuteDto(hostName, query));

            if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to execute WMI query on computer {}: HTTP {}",
                        hostName, response != null ? response.getStatusCode() : "NULL");
            }

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                list = (List<Map<String, Object>>) response.getBody();
            }
          } catch (Exception ex) {
            log.warn("WMI attempt {}/{} failed for {}: {}", attempt, retryAttempts, hostName, ex.getMessage());
          }
          if (list != null || attempt == Math.max(1, retryAttempts)) break;
          try { Thread.sleep(Math.max(0, retryDelayMs)); }
          catch (InterruptedException ex) { Thread.currentThread().interrupt(); break; }
        }

        if (list == null) {
            log.error("WMI client returned null for host {}", hostName);
        }

        return list;
    }

    private void saveHardware(Computer computer, HardwareType hardwareType, List<Map<String, Object>> list,
                              Set<Long> currentHardwareModelIds, Set<Long> processedHardwareModelIds,
                              List<ComputerHardware> previousHardware, Set<Long> matchedHardwareIds,
                              boolean firstHistoryScan) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Map<String, Long> modelOccurrences = new HashMap<>();
        if (hasReliableSerial(hardwareType)) {
            for (Map<String, Object> data : list) {
                modelOccurrences.merge(extractModelName(data, hardwareType), 1L, Long::sum);
            }
        }

        for (Map<String, Object> data : list) {
            String modelName = extractModelName(data, hardwareType);
            HardwareModel hardwareModel = findOrCreateHardwareModel(modelName, hardwareType);

            // Add to current hardware set
            currentHardwareModelIds.add(hardwareModel.getId());
            if (!processedHardwareModelIds.add(hardwareModel.getId())) {
                // This schema stores one computer/model row, not one row for each identical device.
                continue;
            }

            // Check if ComputerHardware already exists
            Optional<ComputerHardware> existingHardware = computerHardwareRepository
                    .findByComputerIdAndHardwareModelId(computer.getId(), hardwareModel.getId());
            if (existingHardware.isEmpty()) {
                existingHardware = matchingPreviousDevice(previousHardware, matchedHardwareIds, hardwareType, data);
                existingHardware.ifPresent(hardware -> hardware.setHardwareModel(hardwareModel));
            }

            ComputerHardware computerHardware;
            boolean newlyInstalled = existingHardware.isEmpty();
            if (existingHardware.isPresent()) {
                // Update existing record
                computerHardware = existingHardware.get();
                matchedHardwareIds.add(computerHardware.getId());
            } else {
                // Create new record
                computerHardware = new ComputerHardware();
                computerHardware.setComputer(computer);
                computerHardware.setHardwareModel(hardwareModel);
            }

            computerHardware = computerHardwareRepository.save(computerHardware);

            if (newlyInstalled) {
                recordChange(computer, hardwareModel, "INSTALLED", null, null, modelName);
            } else if (firstHistoryScan) {
                recordChange(computer, hardwareModel, "FIRST_OBSERVED", null, null, null);
            }

            List<HardwareProperty> existingProperties = newlyInstalled ? List.of()
                    : hardwarePropertyRepository.findByComputerHardwareId(computerHardware.getId());
            if (!newlyInstalled && !firstHistoryScan && hasReliableSerial(hardwareType)
                    && modelOccurrences.getOrDefault(modelName, 0L) == 1L) {
                String previousSerial = propertyValue(existingProperties, "SerialNumber");
                String scannedSerial = mapValue(data, "SerialNumber");
                if (reliableIdentifier(previousSerial) && reliableIdentifier(scannedSerial)
                        && !previousSerial.trim().equalsIgnoreCase(scannedSerial.trim())) {
                    recordChange(computer, hardwareModel, "REPLACED", "SerialNumber",
                            previousSerial.trim(), scannedSerial.trim());
                }
            }
            saveHardwareProperties(computerHardware, data, existingProperties);
        }
    }

    private void removeObsoleteHardware(Computer computer, Set<Long> currentHardwareModelIds) {
        List<ComputerHardware> existingHardware = computerHardwareRepository.findByComputerId(computer.getId());
        
        for (ComputerHardware hardware : existingHardware) {
            if (!currentHardwareModelIds.contains(hardware.getHardwareModel().getId())) {
                log.info("Removing obsolete hardware: {} for computer {}", 
                        hardware.getHardwareModel().getName(), computer.getName());
                recordChange(computer, hardware.getHardwareModel(), "REMOVED", null,
                        hardware.getHardwareModel().getName(), null);
                computerHardwareRepository.delete(hardware);
            }
        }
    }

    private String extractModelName(Map<String, Object> data, HardwareType hardwareType) {
        return switch (hardwareType) {
            case PROCESSOR -> getStringValue(data, "Name");
            case DISK_DRIVE -> getStringValue(data, "Model");
            case VIDEO_CONTROLLER -> getStringValue(data, "Name");
            case BASE_BOARD -> getStringValue(data, "Product");
            case BIOS -> getStringValue(data, "SMBIOSBIOSVersion");
            case OPERATING_SYSTEM -> getStringValue(data, "Caption");
            case PHYSICAL_MEMORY -> getStringValue(data, "PartNumber");
            case DISK_PARTITION -> getStringValue(data, "Name");
            case COMPUTER_SYSTEM -> getStringValue(data, "Model");
            case PATCH -> getStringValue(data, "HotFixID");
        };
    }

    private HardwareModel findOrCreateHardwareModel(String modelName, HardwareType hardwareType) {
        String cacheKey = hardwareType.name() + "_" + modelName;

        return modelCache.computeIfAbsent(cacheKey, k -> {
            Optional<HardwareModel> existingModel = hardwareModelRepository
                    .findByNameAndHardwareType(modelName, hardwareType.toString());
            return existingModel.orElseGet(() -> {
                HardwareModel newModel = new HardwareModel();
                newModel.setName(modelName);
                newModel.setHardwareType(hardwareType.toString());
                return hardwareModelRepository.save(newModel);
            });
        });
    }

    private void saveHardwareProperties(ComputerHardware computerHardware, Map<String, Object> data,
                                        List<HardwareProperty> existingProperties) {
        Set<String> processedProperties = new HashSet<>();

        Map<String, HardwareProperty> existingPropertiesMap = new HashMap<>();
        for (HardwareProperty prop : existingProperties) {
            existingPropertiesMap.put(prop.getPropertyName(), prop);
        }

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            if (propertyValue == null || processedProperties.contains(propertyName)) {
                continue;
            }

            processedProperties.add(propertyName);

            String propertyValueStr = propertyValue.toString();
            
            // Check if property already exists
            HardwareProperty existingProperty = existingPropertiesMap.get(propertyName);
            if (existingProperty != null) {
                // Update existing property if value changed
                if (!existingProperty.getPropertyValue().equals(propertyValueStr)) {
                    existingProperty.setPropertyValue(propertyValueStr);
                    hardwarePropertyRepository.save(existingProperty);
                    log.debug("Updated property {} for hardware {}", propertyName, computerHardware.getId());
                }
            } else {
                // Create new property
                HardwareValue hardwareValue = findOrCreateHardwareValue(computerHardware.getComputer(), propertyValueStr);

                HardwareProperty property = new HardwareProperty();
                property.setComputerHardware(computerHardware);
                property.setHardwareValue(hardwareValue);
                property.setPropertyName(propertyName);
                property.setPropertyValue(propertyValueStr);

                hardwarePropertyRepository.save(property);
                log.debug("Created new property {} for hardware {}", propertyName, computerHardware.getId());
            }
        }

        for (HardwareProperty existingProperty : existingPropertiesMap.values()) {
            if (data.containsKey(existingProperty.getPropertyName())
                    && data.get(existingProperty.getPropertyName()) == null) {
                hardwarePropertyRepository.delete(existingProperty);
            }
        }
    }

    private boolean hasReliableSerial(HardwareType type) {
        return type == HardwareType.DISK_DRIVE || type == HardwareType.PHYSICAL_MEMORY
                || type == HardwareType.BASE_BOARD;
    }

    private Optional<ComputerHardware> matchingPreviousDevice(List<ComputerHardware> previousHardware,
            Set<Long> matchedHardwareIds, HardwareType type, Map<String, Object> scannedValues) {
        String identifierName = switch (type) {
            case DISK_DRIVE, PHYSICAL_MEMORY, BASE_BOARD -> "SerialNumber";
            case VIDEO_CONTROLLER -> "PNPDeviceID";
            case PROCESSOR -> "ProcessorId";
            default -> null;
        };
        if (identifierName == null) return Optional.empty();
        String scannedIdentifier = mapValue(scannedValues, identifierName);
        if (!reliableIdentifier(scannedIdentifier)) return Optional.empty();
        for (ComputerHardware previous : previousHardware) {
            if (matchedHardwareIds.contains(previous.getId())
                    || !type.toString().equals(previous.getHardwareModel().getHardwareType())) {
                continue;
            }
            String previousIdentifier = propertyValue(
                    hardwarePropertyRepository.findByComputerHardwareId(previous.getId()), identifierName);
            if (reliableIdentifier(previousIdentifier)
                    && previousIdentifier.trim().equalsIgnoreCase(scannedIdentifier.trim())) {
                return Optional.of(previous);
            }
        }
        return Optional.empty();
    }

    private String propertyValue(List<HardwareProperty> properties, String name) {
        return properties.stream().filter(property -> name.equalsIgnoreCase(property.getPropertyName()))
                .map(HardwareProperty::getPropertyValue).findFirst().orElse(null);
    }

    private String mapValue(Map<String, Object> values, String name) {
        return values.entrySet().stream().filter(entry -> name.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue).filter(Objects::nonNull).map(Object::toString)
                .findFirst().orElse(null);
    }

    private boolean reliableIdentifier(String value) {
        if (value == null || value.isBlank()) return false;
        String normalized = value.trim();
        String alphanumeric = normalized.replaceAll("[^0-9A-Za-z]", "");
        return !alphanumeric.isEmpty()
                && !Set.of("unknown", "none", "notavailable", "notspecified", "notapplicable",
                        "tobefilledbyoem", "na", "null", "defaultstring", "systemserialnumber")
                        .contains(alphanumeric.toLowerCase(Locale.ROOT))
                && !alphanumeric.matches("0+");
    }

    private void recordChange(Computer computer, HardwareModel model, String changeType,
                              String propertyName, String oldValue, String newValue) {
        if (model == null || "Unknown".equalsIgnoreCase(model.getName())
                || "Patch".equals(model.getHardwareType())
                || "OperatingSystem".equals(model.getHardwareType())
                || "BIOS".equals(model.getHardwareType())
                || "ComputerSystem".equals(model.getHardwareType())) {
            return;
        }
        HardwareChange change = new HardwareChange();
        change.setComputer(computer);
        change.setHardwareModel(model);
        change.setChangeType(changeType);
        change.setPropertyName(propertyName);
        change.setOldValue(oldValue);
        change.setNewValue(newValue);
        change.setChangedAt(LocalDateTime.now());
        hardwareChangeRepository.save(change);
    }

    private HardwareValue findOrCreateHardwareValue(Computer computer, String propertyValue) {
        Optional<HardwareValue> existingValue = hardwareValueRepository
                .findByComputerAndPropertyValue(computer, propertyValue);
        return existingValue.orElseGet(() -> {
            HardwareValue newValue = new HardwareValue();
            newValue.setComputer(computer);
            newValue.setPropertyValue(propertyValue);
            return hardwareValueRepository.save(newValue);
        });
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "Unknown";
    }

}
