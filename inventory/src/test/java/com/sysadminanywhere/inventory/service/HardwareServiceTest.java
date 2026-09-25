package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
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
}
