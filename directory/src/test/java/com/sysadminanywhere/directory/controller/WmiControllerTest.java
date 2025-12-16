package com.sysadminanywhere.directory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.directory.service.WmiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WmiControllerTest {

    @Mock
    private WmiService wmiService;

    @InjectMocks
    private WmiController wmiController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wmiController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void execute_returnsWmiResults() throws Exception {
        ExecuteDto executeDto = new ExecuteDto();
        executeDto.setHostName("server01.example.com");
        executeDto.setWqlQuery("SELECT * FROM Win32_Process");

        List<Map<String, Object>> wmiResult = List.of(Map.of("Name", "TestProcess", "ProcessId", 1234));
        when(wmiService.execute(anyString(), anyString())).thenReturn(wmiResult);

        mockMvc.perform(post("/api/wmi/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].Name").value("TestProcess"))
                .andExpect(jsonPath("$[0].ProcessId").value(1234));

        verify(wmiService).execute("server01.example.com", "SELECT * FROM Win32_Process");
    }

    @Test
    void execute_returnsBadRequestOnException() throws Exception {
        ExecuteDto executeDto = new ExecuteDto();
        executeDto.setHostName("server01.example.com");
        executeDto.setWqlQuery("SELECT * FROM Win32_Process");

        when(wmiService.execute(anyString(), anyString())).thenThrow(new RuntimeException("Connection failed"));

        mockMvc.perform(post("/api/wmi/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(wmiService).execute("server01.example.com", "SELECT * FROM Win32_Process");
    }

    @Test
    void clearExecuteCache_clearsCache() throws Exception {
        ExecuteDto executeDto = new ExecuteDto();
        executeDto.setHostName("server01.example.com");
        executeDto.setWqlQuery("SELECT * FROM Win32_Process");

        doNothing().when(wmiService).clearExecuteCache(anyString(), anyString());

        mockMvc.perform(post("/api/wmi/execute/clear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeDto)))
                .andExpect(status().isOk());

        verify(wmiService).clearExecuteCache("server01.example.com", "SELECT * FROM Win32_Process");
    }

    @Test
    void invoke_returnsInvokeResult() throws Exception {
        InvokeDto invokeDto = new InvokeDto();
        invokeDto.setHostName("server01.example.com");
        invokeDto.setPath("root\\cimv2");
        invokeDto.setClassName("Win32_Process");
        invokeDto.setMethodName("Create");
        invokeDto.setInputMap(Map.of("CommandLine", "notepad.exe"));

        Map<String, Object> invokeResult = Map.of("ReturnValue", 0, "ProcessId", 5678);
        when(wmiService.invoke(anyString(), anyString(), anyString(), anyString(), anyMap())).thenReturn(invokeResult);

        mockMvc.perform(post("/api/wmi/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invokeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ReturnValue").value(0))
                .andExpect(jsonPath("$.ProcessId").value(5678));

        verify(wmiService).invoke("server01.example.com", "root\\cimv2", "Win32_Process", "Create", Map.of("CommandLine", "notepad.exe"));
    }

    @Test
    void invoke_returnsBadRequestOnException() throws Exception {
        InvokeDto invokeDto = new InvokeDto();
        invokeDto.setHostName("server01.example.com");
        invokeDto.setPath("root\\cimv2");
        invokeDto.setClassName("Win32_Process");
        invokeDto.setMethodName("Create");
        invokeDto.setInputMap(Map.of("CommandLine", "notepad.exe"));

        when(wmiService.invoke(anyString(), anyString(), anyString(), anyString(), anyMap()))
                .thenThrow(new RuntimeException("Invoke failed"));

        mockMvc.perform(post("/api/wmi/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invokeDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isEmpty());

        verify(wmiService).invoke(anyString(), anyString(), anyString(), anyString(), anyMap());
    }
}
