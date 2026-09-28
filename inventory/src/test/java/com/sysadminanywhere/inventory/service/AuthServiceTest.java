package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.inventory.client.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test
    void retriesTransientDirectoryOutage() {
        AuthServiceClient client = mock(AuthServiceClient.class);
        when(client.authenticate(any())).thenThrow(new ResourceAccessException("offline"))
                .thenReturn(new JwtResponse("token", "readonly", java.util.List.of("ROLE_READER")));

        JwtResponse response = new AuthService(client, 3, 0).authenticate("readonly", "password");

        assertEquals("token", response.token());
        verify(client, times(2)).authenticate(any());
    }

    @Test
    void stopsAfterConfiguredAttempts() {
        AuthServiceClient client = mock(AuthServiceClient.class);
        when(client.authenticate(any())).thenThrow(new ResourceAccessException("offline"));

        assertThrows(IllegalStateException.class,
                () -> new AuthService(client, 2, 0).authenticate("readonly", "password"));
        verify(client, times(2)).authenticate(any());
    }
}
