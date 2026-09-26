package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.ComputerHardware;
import com.sysadminanywhere.inventory.entity.HardwareChange;
import com.sysadminanywhere.inventory.entity.HardwareModel;
import com.sysadminanywhere.inventory.entity.HardwareProperty;
import com.sysadminanywhere.inventory.entity.HardwareValue;
import com.sysadminanywhere.inventory.repository.ComputerHardwareRepository;
import com.sysadminanywhere.inventory.repository.HardwareModelRepository;
import com.sysadminanywhere.inventory.repository.HardwarePropertyRepository;
import com.sysadminanywhere.inventory.repository.HardwareValueRepository;
import com.sysadminanywhere.inventory.repository.HardwareChangeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HardwareServiceTest {

    @Mock
    private WmiServiceClient wmiServiceClient;

    @Mock
    private ComputerHardwareRepository computerHardwareRepository;

    @Mock
    private HardwareModelRepository hardwareModelRepository;

    @Mock
    private HardwarePropertyRepository hardwarePropertyRepository;

    @Mock
    private HardwareValueRepository hardwareValueRepository;

    @Mock
    private HardwareChangeRepository hardwareChangeRepository;

    @InjectMocks
    private HardwareService hardwareService;

    @Test
    void scanHardware_shouldHandleWmiFailure() {
        Computer computer = new Computer();
        computer.setId(1L);
        computer.setName("PC001");

        // When WMI fails (returns null), the service should handle it gracefully
        when(wmiServiceClient.execute(any(ExecuteDto.class))).thenReturn(null);
        hardwareService.scanHardware(computer);

        // Should attempt all hardware and Windows patch WMI queries
        verify(wmiServiceClient, times(9)).execute(any(ExecuteDto.class));
        verify(computerHardwareRepository, never()).delete(any());
    }

    @Test
    void changingOrdinaryWmiPropertyDoesNotCreateHardwareEvent() {
        Computer computer = computer();
        ComputerHardware disk = existingDisk(computer);
        HardwareProperty serial = property(disk, "SerialNumber", "SN001");
        HardwareProperty size = property(disk, "Size", "100");
        stubExistingDisk(disk, List.of(serial, size),
                List.of(Map.of("Model", "Drive A", "SerialNumber", "SN001", "Size", "120")));

        hardwareService.scanHardware(computer);

        verify(hardwareChangeRepository, never()).save(any());
        verify(hardwarePropertyRepository).save(size);
        assertEquals("120", size.getPropertyValue());
    }

    @Test
    void changedReliableSerialRecordsOneReplacement() {
        Computer computer = computer();
        ComputerHardware disk = existingDisk(computer);
        HardwareProperty serial = property(disk, "SerialNumber", "SN001");
        HardwareProperty size = property(disk, "Size", "100");
        stubExistingDisk(disk, List.of(serial, size),
                List.of(Map.of("Model", "Drive A", "SerialNumber", "SN002", "Size", "100")));

        hardwareService.scanHardware(computer);

        ArgumentCaptor<HardwareChange> changes = ArgumentCaptor.forClass(HardwareChange.class);
        verify(hardwareChangeRepository).save(changes.capture());
        assertEquals("REPLACED", changes.getValue().getChangeType());
        assertEquals("SN001", changes.getValue().getOldValue());
        assertEquals("SN002", changes.getValue().getNewValue());
    }

    @Test
    void duplicateSameModelDevicesDoNotImplyReplacement() {
        Computer computer = computer();
        ComputerHardware disk = existingDisk(computer);
        HardwareProperty serial = property(disk, "SerialNumber", "SN001");
        stubExistingDisk(disk, List.of(serial), List.of(
                        Map.of("Model", "Drive A", "SerialNumber", "SN002"),
                        Map.of("Model", "Drive A", "SerialNumber", "SN001")));

        hardwareService.scanHardware(computer);

        verify(hardwareChangeRepository, never()).save(any());
    }

    @Test
    void changedWmiModelNameWithSameSerialIsNotHardwareChange() {
        Computer computer = computer();
        ComputerHardware disk = existingDisk(computer);
        HardwareProperty serial = property(disk, "SerialNumber", "SN001");
        HardwareModel renamedModel = new HardwareModel();
        renamedModel.setId(4L);
        renamedModel.setName("Drive B");
        renamedModel.setHardwareType("DiskDrive");
        when(hardwareChangeRepository.existsByComputerIdAndChangeTypeNot(1L, "CONFIGURATION_CHANGED"))
                .thenReturn(true);
        when(wmiServiceClient.execute(any(ExecuteDto.class))).thenAnswer(invocation -> {
            ExecuteDto request = invocation.getArgument(0);
            if (request.getWqlQuery().contains("Win32_DiskDrive")) {
                return ResponseEntity.ok(List.of(Map.of("Model", "Drive B", "SerialNumber", "SN001")));
            }
            return ResponseEntity.ok(List.<Map<String, Object>>of());
        });
        when(hardwareModelRepository.findByNameAndHardwareType("Drive B", "DiskDrive"))
                .thenReturn(Optional.of(renamedModel));
        when(computerHardwareRepository.findByComputerId(1L)).thenReturn(List.of(disk));
        when(computerHardwareRepository.save(disk)).thenReturn(disk);
        when(hardwarePropertyRepository.findByComputerHardwareId(3L)).thenReturn(List.of(serial));

        hardwareService.scanHardware(computer);

        assertEquals(renamedModel, disk.getHardwareModel());
        verify(hardwareChangeRepository, never()).save(any());
        verify(computerHardwareRepository, never()).delete(any());
    }

    @Test
    void successfulScanRecordsRemovalOfMissingModel() {
        Computer computer = computer();
        ComputerHardware disk = existingDisk(computer);
        when(hardwareChangeRepository.existsByComputerIdAndChangeTypeNot(1L, "CONFIGURATION_CHANGED"))
                .thenReturn(true);
        when(wmiServiceClient.execute(any(ExecuteDto.class)))
                .thenAnswer(invocation -> ResponseEntity.ok(List.<Map<String, Object>>of()));
        when(computerHardwareRepository.findByComputerId(1L)).thenReturn(List.of(disk));

        hardwareService.scanHardware(computer);

        ArgumentCaptor<HardwareChange> changes = ArgumentCaptor.forClass(HardwareChange.class);
        verify(hardwareChangeRepository).save(changes.capture());
        assertEquals("REMOVED", changes.getValue().getChangeType());
        verify(computerHardwareRepository).delete(disk);
    }

    private void stubExistingDisk(ComputerHardware disk, List<HardwareProperty> properties,
                                  List<Map<String, String>> diskRows) {
        when(hardwareChangeRepository.existsByComputerIdAndChangeTypeNot(1L, "CONFIGURATION_CHANGED"))
                .thenReturn(true);
        when(wmiServiceClient.execute(any(ExecuteDto.class))).thenAnswer(invocation -> {
            ExecuteDto request = invocation.getArgument(0);
            if (request.getWqlQuery().contains("Win32_DiskDrive")) {
                return ResponseEntity.ok(diskRows);
            }
            return ResponseEntity.ok(List.of());
        });
        when(hardwareModelRepository.findByNameAndHardwareType("Drive A", "DiskDrive"))
                .thenReturn(Optional.of(disk.getHardwareModel()));
        when(computerHardwareRepository.findByComputerIdAndHardwareModelId(1L, 2L))
                .thenReturn(Optional.of(disk));
        when(computerHardwareRepository.save(disk)).thenReturn(disk);
        when(computerHardwareRepository.findByComputerId(1L)).thenReturn(List.of(disk));
        when(hardwarePropertyRepository.findByComputerHardwareId(3L)).thenReturn(properties);
    }

    private Computer computer() {
        Computer computer = new Computer();
        computer.setId(1L);
        computer.setName("PC001");
        return computer;
    }

    private ComputerHardware existingDisk(Computer computer) {
        HardwareModel model = new HardwareModel();
        model.setId(2L);
        model.setName("Drive A");
        model.setHardwareType("DiskDrive");
        ComputerHardware disk = new ComputerHardware();
        disk.setId(3L);
        disk.setComputer(computer);
        disk.setHardwareModel(model);
        return disk;
    }

    private HardwareProperty property(ComputerHardware disk, String name, String value) {
        HardwareProperty property = new HardwareProperty();
        property.setComputerHardware(disk);
        property.setPropertyName(name);
        property.setPropertyValue(value);
        property.setHardwareValue(new HardwareValue());
        return property;
    }
}
