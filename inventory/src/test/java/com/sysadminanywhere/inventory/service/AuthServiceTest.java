package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.dto.LoginRequest;
import com.sysadminanywhere.inventory.client.AuthServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void authenticate_withValidCredentials_returnsJwtResponse() {
        JwtResponse expectedResponse = new JwtResponse("test-token", "testuser", List.of("ROLE_USER"));
        when(authServiceClient.authenticate(any(LoginRequest.class))).thenReturn(expectedResponse);

        JwtResponse result = authService.authenticate("testuser", "testpass");

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("test-token");
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.roles()).contains("ROLE_USER");
    }

    @Test
    void authenticate_withNullResponse_throwsException() {
        when(authServiceClient.authenticate(any(LoginRequest.class))).thenReturn(null);

        assertThatThrownBy(() -> authService.authenticate("testuser", "testpass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authentication failed");
    }

    @Test
    void authenticate_withNullToken_throwsException() {
        JwtResponse responseWithNullToken = new JwtResponse(null, "testuser", List.of("ROLE_USER"));
        when(authServiceClient.authenticate(any(LoginRequest.class))).thenReturn(responseWithNullToken);

        assertThatThrownBy(() -> authService.authenticate("testuser", "testpass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authentication failed");
    }

    @Test
    void authenticate_withEmptyToken_returnsResponse() {
        JwtResponse responseWithEmptyToken = new JwtResponse("", "testuser", List.of("ROLE_USER"));
        when(authServiceClient.authenticate(any(LoginRequest.class))).thenReturn(responseWithEmptyToken);

        JwtResponse result = authService.authenticate("testuser", "testpass");

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("");
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.roles()).contains("ROLE_USER");
    }
}
