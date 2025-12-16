package com.sysadminanywhere.directory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WmiServiceTest {

    @Mock
    private LdapService ldapService;

    private WmiService wmiService;

    @BeforeEach
    void setUp() {
        wmiService = new WmiService(ldapService);
        ReflectionTestUtils.setField(wmiService, "userName", "testuser");
        ReflectionTestUtils.setField(wmiService, "password", "testpassword");
    }

    @Test
    void checkHostName_returnsHostNameUnchanged() {
        when(ldapService.getDomainName()).thenReturn("example.com");

        String result = ReflectionTestUtils.invokeMethod(wmiService, "checkHostName", "server01");

        assertThat(result).isEqualTo("server01");
    }

    @Test
    void checkHostName_returnsHostNameWithDomainUnchanged() {
        when(ldapService.getDomainName()).thenReturn("example.com");

        String result = ReflectionTestUtils.invokeMethod(wmiService, "checkHostName", "server01.example.com");

        assertThat(result).isEqualTo("server01.example.com");
    }

    @Test
    void clearExecuteCache_doesNotThrowException() {
        wmiService.clearExecuteCache("server01", "SELECT * FROM Win32_Process");
    }
}
