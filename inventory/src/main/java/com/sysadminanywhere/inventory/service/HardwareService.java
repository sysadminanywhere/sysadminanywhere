package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.model.HardwareType;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.HardwareModelRepository;
import com.sysadminanywhere.inventory.repository.HardwarePropertyRepository;
import com.sysadminanywhere.inventory.repository.HardwareValueRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sysadminanywhere.inventory.entity.*;
import java.time.LocalDateTime;
import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HardwareService {

    private final WmiServiceClient wmiServiceClient;
    private final ComputerHardwareRepository computerHardwareRepository;
    private final HardwareModelRepository hardwareModelRepository;
    private final HardwarePropertyRepository hardwarePropertyRepository;
    private final HardwareValueRepository hardwareValueRepository;

    private final Map<String, HardwareModel> modelCache = new HashMap<>();

    public HardwareService(WmiServiceClient wmiServiceClient,
                           ComputerHardwareRepository computerHardwareRepository,
                           HardwareModelRepository hardwareModelRepository,
                           HardwarePropertyRepository hardwarePropertyRepository,
                           HardwareValueRepository hardwareValueRepository) {

        this.wmiServiceClient = wmiServiceClient;
        this.computerHardwareRepository = computerHardwareRepository;
        this.hardwareModelRepository = hardwareModelRepository;
        this.hardwarePropertyRepository = hardwarePropertyRepository;
        this.hardwareValueRepository = hardwareValueRepository;
    }
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transactional
    public void scanHardware(Computer computer) {
        String hostName = computer.getName();
        log.info("Scanning hardware on computer {}", hostName);

        List<Map<String, Object>> diskDrives = execute(hostName, "SELECT * FROM Win32_DiskDrive");
        List<Map<String, Object>> operatingSystems = execute(hostName, "SELECT * FROM Win32_OperatingSystem");
        List<Map<String, Object>> processors = execute(hostName, "SELECT * FROM Win32_Processor");
        List<Map<String, Object>> videoControllers = execute(hostName, "SELECT * FROM Win32_VideoController");
        List<Map<String, Object>> physicalMemory = execute(hostName, "SELECT * FROM Win32_PhysicalMemory");
        List<Map<String, Object>> baseBoards = execute(hostName, "SELECT * FROM Win32_BaseBoard");
        List<Map<String, Object>> bios = execute(hostName, "SELECT * FROM Win32_BIOS");
        List<Map<String, Object>> computerSystems = execute(hostName, "SELECT * FROM Win32_ComputerSystem");

        saveHardware(computer, HardwareType.DISK_DRIVE, diskDrives);
        saveHardware(computer, HardwareType.OPERATING_SYSTEM, operatingSystems);
        saveHardware(computer, HardwareType.PROCESSOR, processors);
        saveHardware(computer, HardwareType.VIDEO_CONTROLLER, videoControllers);
        saveHardware(computer, HardwareType.PHYSICAL_MEMORY, physicalMemory);
        saveHardware(computer, HardwareType.BASE_BOARD, baseBoards);
        saveHardware(computer, HardwareType.BIOS, bios);
        saveHardware(computer, HardwareType.COMPUTER_SYSTEM, computerSystems);
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

    private void saveHardware(Computer computer, HardwareType hardwareType, List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Map<String, Object> data : list) {
            String modelName = extractModelName(data, hardwareType);
            HardwareModel hardwareModel = findOrCreateHardwareModel(modelName, hardwareType);

            // Check if ComputerHardware already exists
            Optional<ComputerHardware> existingHardware = computerHardwareRepository
                    .findByComputerIdAndHardwareModelId(computer.getId(), hardwareModel.getId());

            ComputerHardware computerHardware;
            if (existingHardware.isPresent()) {
                // Update existing record
                computerHardware = existingHardware.get();
            } else {
                // Create new record
                computerHardware = new ComputerHardware();
                computerHardware.setComputer(computer);
                computerHardware.setHardwareModel(hardwareModel);
            }

            computerHardware = computerHardwareRepository.save(computerHardware);

            saveHardwareProperties(computerHardware, data, hardwareType);
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
        };
    }

    private HardwareModel findOrCreateHardwareModel(String modelName, HardwareType hardwareType) {
        String cacheKey = hardwareType.name() + "_" + modelName;

        return modelCache.computeIfAbsent(cacheKey, k -> {
            List<HardwareModel> existingModels = hardwareModelRepository.findAll();
            return existingModels.stream()
                    .filter(model -> modelName.equals(model.getName()) &&
                            hardwareType.toString().equals(model.getHardwareType()))
                    .findFirst()
                    .orElseGet(() -> {
                        HardwareModel newModel = new HardwareModel();
                        newModel.setName(modelName);
                        newModel.setHardwareType(hardwareType.toString());
                        return hardwareModelRepository.save(newModel);
                    });
        });
    }

    private void saveHardwareProperties(ComputerHardware computerHardware, Map<String, Object> data, HardwareType hardwareType) {
        Set<String> processedProperties = new HashSet<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            if (propertyValue == null || processedProperties.contains(propertyName)) {
                continue;
            }

            processedProperties.add(propertyName);

            String propertyValueStr = propertyValue.toString();
            HardwareValue hardwareValue = findOrCreateHardwareValue(computerHardware.getComputer(), propertyValueStr);

            HardwareProperty property = new HardwareProperty();
            property.setComputerHardware(computerHardware);
            property.setHardwareValue(hardwareValue);
            property.setPropertyName(propertyName);
            property.setPropertyValue(propertyValueStr);

            hardwarePropertyRepository.save(property);
        }
    }

    private HardwareValue findOrCreateHardwareValue(Computer computer, String propertyValue) {
        List<HardwareValue> existingValues = hardwareValueRepository.findAll();
        return existingValues.stream()
                .filter(value -> computer.equals(value.getComputer()) &&
                        propertyValue.equals(value.getPropertyValue()))
                .findFirst()
                .orElseGet(() -> {
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
