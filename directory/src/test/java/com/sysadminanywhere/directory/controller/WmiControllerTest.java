package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.directory.service.WmiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WmiControllerTest {

    @Mock
    private WmiService wmiService;

    @InjectMocks
    private WmiController wmiController;

    @Test
    void execute_shouldReturnResult() throws Exception {
        ExecuteDto dto = new ExecuteDto("PC001", "SELECT * FROM Win32_Process");
        List<Map<String, Object>> result = List.of(Map.of("Name", "notepad.exe"));
        lenient().when(wmiService.execute(anyString(), anyString())).thenReturn(result);

        ResponseEntity<?> response = wmiController.execute(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void clearExecuteCache_shouldReturnOk() {
        ExecuteDto dto = new ExecuteDto("PC001", "SELECT * FROM Win32_Process");
        lenient().doNothing().when(wmiService).clearExecuteCache(anyString(), anyString());

        ResponseEntity<?> response = wmiController.clearExecuteCache(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void invoke_shouldReturnResult() throws Exception {
        InvokeDto dto = new InvokeDto();
        dto.setHostName("PC001");
        dto.setClassName("Win32_Process");
        dto.setMethodName("Create");
        dto.setInputMap(Map.of("CommandLine", "notepad.exe"));

        Map<String, Object> result = Map.of("ReturnValue", 0);
        lenient().when(wmiService.invoke(anyString(), any(), anyString(), anyString(), anyMap()))
            .thenReturn(result);

        ResponseEntity<?> response = wmiController.invoke(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void command_shouldReturnOk() throws Exception {
        CommandDto dto = new CommandDto();
        dto.setHostName("PC001");
        dto.setCommand("dir");

        lenient().doNothing().when(wmiService).executeCommand(anyString(), anyString(), any());

        ResponseEntity<?> response = wmiController.command(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
