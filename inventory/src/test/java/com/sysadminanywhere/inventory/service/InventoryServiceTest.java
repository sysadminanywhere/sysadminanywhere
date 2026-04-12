package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.inventory.entity.Computer;
import com.sysadminanywhere.inventory.repository.ComputerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ComputerRepository computerRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void checkComputer_shouldCreateNewComputerWhenNotFound() {
        String hostName = "PC001";
        when(computerRepository.findAllByName(hostName)).thenReturn(Collections.emptyList());
        when(computerRepository.save(any(Computer.class))).thenAnswer(invocation -> {
            Computer c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        Computer result = inventoryService.checkComputer(hostName);

        assertNotNull(result);
        assertEquals(hostName, result.getName());
        assertNotNull(result.getCheckingDate());
        verify(computerRepository).save(any(Computer.class));
    }

    @Test
    void checkComputer_shouldReturnExistingComputer() {
        String hostName = "PC001";
        Computer existingComputer = new Computer();
        existingComputer.setId(1L);
        existingComputer.setName(hostName);
        existingComputer.setCheckingDate(LocalDateTime.now().minusDays(1));

        when(computerRepository.findAllByName(hostName)).thenReturn(List.of(existingComputer));

        Computer result = inventoryService.checkComputer(hostName);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(hostName, result.getName());
        verify(computerRepository, never()).save(any());
    }

    @Test
    void checkComputer_shouldHandleNullHostName() {
        Computer result = inventoryService.checkComputer(null);

        assertNull(result);
        verify(computerRepository, never()).save(any());
    }

    @Test
    void checkComputer_shouldHandleEmptyHostName() {
        Computer result = inventoryService.checkComputer("");

        assertNull(result);
        verify(computerRepository, never()).save(any());
    }
}
