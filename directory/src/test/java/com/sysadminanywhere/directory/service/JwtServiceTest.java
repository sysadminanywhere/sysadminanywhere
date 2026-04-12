package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.directory.service.JwtService.JwtPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final String secret = "mySecretKeyForJwtTokenGenerationThatIsLongEnough123456789012345678901234567890";

    @Test
    void generateToken_shouldCreateValidToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token = jwtService.generateToken("testuser", List.of("ROLE_USER"), "test-service");

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateToken_shouldContainCorrectUsername() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token = jwtService.generateToken("admin", List.of("ROLE_ADMIN"), "web");
        
        JwtPrincipal principal = jwtService.parseAndValidate(token);
        
        assertEquals("admin", principal.username());
    }

    @Test
    void generateToken_shouldContainCorrectRoles() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        String token = jwtService.generateToken("user", roles, "web");
        
        JwtPrincipal principal = jwtService.parseAndValidate(token);
        
        assertEquals(2, principal.roles().size());
    }

    @Test
    void generateToken_shouldContainCorrectService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token = jwtService.generateToken("user", List.of("ROLE_USER"), "inventory");
        
        JwtPrincipal principal = jwtService.parseAndValidate(token);
        
        assertEquals("inventory", principal.service());
    }

    @Test
    void generateToken_shouldHandleEmptyRoles() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token = jwtService.generateToken("user", Collections.emptyList(), "service");
        
        JwtPrincipal principal = jwtService.parseAndValidate(token);
        
        assertTrue(principal.roles().isEmpty());
    }

    @Test
    void generateToken_shouldHandleNullService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token = jwtService.generateToken("user", List.of("ROLE_USER"), null);
        
        JwtPrincipal principal = jwtService.parseAndValidate(token);
        
        assertNull(principal.service());
    }

    @Test
    void parseAndValidate_shouldRejectInvalidToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        assertThrows(Exception.class, () -> {
            jwtService.parseAndValidate("invalid.token.here");
        });
    }

    @Test
    void parseAndValidate_shouldRejectEmptyToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        assertThrows(Exception.class, () -> {
            jwtService.parseAndValidate("");
        });
    }

    @Test
    void parseAndValidate_shouldRejectTamperedToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String validToken = jwtService.generateToken("user", List.of("ROLE_USER"), "service");
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";
        
        assertThrows(Exception.class, () -> {
            jwtService.parseAndValidate(tamperedToken);
        });
    }

    @Test
    void generateToken_differentUsers_shouldCreateUniqueTokens() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        
        String token1 = jwtService.generateToken("user1", List.of("ROLE_USER"), "service");
        String token2 = jwtService.generateToken("user2", List.of("ROLE_USER"), "service");
        
        assertNotEquals(token1, token2);
    }
}
