package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.inventory.client.WmiServiceClient;
import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.entity.Installation;
import com.sysadminanywhere.inventory.entity.Software;
import com.sysadminanywhere.inventory.model.wmi.SoftwareEntity;
import com.sysadminanywhere.inventory.repository.InstallationRepository;
import com.sysadminanywhere.inventory.repository.SoftwareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoftwareServiceTest {

    @Mock
    private WmiServiceClient wmiServiceClient;

    @Mock
    private SoftwareRepository softwareRepository;

    @Mock
    private InstallationRepository installationRepository;

    @InjectMocks
    private SoftwareService softwareService;

    @Test
    void checkSoftware_shouldCreateNewSoftwareWhenNotFound() {
        SoftwareEntity entity = new SoftwareEntity();
        entity.setName("Test Software");
        entity.setVendor("Test Vendor");
        entity.setVersion("1.0");

        when(softwareRepository.findByNameAndVendor("Test Software", "Test Vendor"))
            .thenReturn(Collections.emptyList());
        when(softwareRepository.save(any(Software.class))).thenAnswer(invocation -> {
            Software s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        Software result = softwareService.checkSoftware(entity);

        assertNotNull(result);
        assertEquals("Test Software", result.getName());
        verify(softwareRepository).save(any(Software.class));
    }

    @Test
    void checkSoftware_shouldHandleNullEntity() {
        Software result = softwareService.checkSoftware(null);

        assertNull(result);
        verify(softwareRepository, never()).save(any());
    }

    @Test
    void checkSoftware_shouldHandleNullName() {
        SoftwareEntity entity = new SoftwareEntity();
        entity.setName(null);

        Software result = softwareService.checkSoftware(entity);

        assertNull(result);
        verify(softwareRepository, never()).save(any());
    }

    @Test
    void scanSoftware_shouldHandleWmiFailure() {
        Computer computer = new Computer();
        computer.setName("PC001");

        when(wmiServiceClient.execute(any(ExecuteDto.class))).thenReturn(null);

        softwareService.scanSoftware(computer);

        verify(wmiServiceClient).execute(any(ExecuteDto.class));
        verify(softwareRepository, never()).save(any());
    }
}
