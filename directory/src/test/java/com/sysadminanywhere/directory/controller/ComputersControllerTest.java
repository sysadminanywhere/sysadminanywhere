package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.PageResponse;
import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.directory.service.ComputersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComputersControllerTest {

    @Mock
    private ComputersService computersService;

    @InjectMocks
    private ComputersController computersController;

    @Test
    void getAll_shouldReturnPageOfComputers() {
        Page<ComputerEntry> page = new PageImpl<>(List.of(new ComputerEntry()));
        when(computersService.getAll(any(PageRequest.class), anyString(), any(String[].class))).thenReturn(page);

        ResponseEntity<PageResponse<ComputerEntry>> result = computersController.getAll(0, 10, "", "filter", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(computersService).getAll(any(PageRequest.class), eq("filter"), any(String[].class));
    }

    @Test
    void getList_shouldReturnListOfComputers() {
        List<ComputerEntry> computers = List.of(new ComputerEntry(), new ComputerEntry());
        when(computersService.getAll(anyString(), any(String[].class))).thenReturn(computers);

        ResponseEntity<List<ComputerEntry>> result = computersController.getList("", new String[]{"cn"});

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getByCN_shouldReturnComputer() {
        ComputerEntry computer = new ComputerEntry();
        computer.setCn("PC001");
        when(computersService.getByCN("PC001")).thenReturn(computer);

        ResponseEntity<ComputerEntry> result = computersController.getByCN("PC001");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("PC001", result.getBody().getCn());
    }

    @Test
    void getByCN_shouldReturnBadRequestForBlankCN() {
        ResponseEntity<ComputerEntry> result = computersController.getByCN("");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        verifyNoInteractions(computersService);
    }

    @Test
    void add_shouldCreateComputer() {
        AddComputerDto dto = new AddComputerDto();
        dto.setDistinguishedName("CN=PC002,OU=Computers,DC=com");
        dto.setCn("PC002");
        dto.setDescription("Test PC");
        dto.setLocation("Office");
        dto.setEnabled(true);

        ComputerEntry computer = new ComputerEntry();
        computer.setCn("PC002");
        when(computersService.add(anyString(), anyString(), any(), any(), anyBoolean()))
            .thenReturn(computer);

        ResponseEntity<ComputerEntry> result = computersController.add(dto);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(computersService).add(anyString(), eq("PC002"), any(), any(), eq(true));
    }

    @Test
    void add_shouldReturnBadRequestForNullDto() {
        ResponseEntity<ComputerEntry> result = computersController.add(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        verifyNoInteractions(computersService);
    }

    @Test
    void delete_shouldCallServiceDelete() {
        String dn = "CN=PC001,OU=Computers,DC=example,DC=com";
        doNothing().when(computersService).delete(dn);

        ResponseEntity<?> result = computersController.delete(dn);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(computersService).delete(dn);
    }
}
