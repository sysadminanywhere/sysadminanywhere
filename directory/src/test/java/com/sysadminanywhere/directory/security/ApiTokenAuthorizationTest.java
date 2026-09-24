package com.sysadminanywhere.directory.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiTokenAuthorizationTest {
    private final ApiTokenAuthorization policy = new ApiTokenAuthorization();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void readerCanSearchButCannotWriteOrRunCommands() {
        authenticate("ROLE_READER");
        request("GET", "/api/users");
        assertTrue(policy.isAllowed());
        request("POST", "/api/ldap/search");
        assertTrue(policy.isAllowed());
        request("POST", "/api/users/bulk/delete");
        assertFalse(policy.isAllowed());
        request("POST", "/api/wmi/command");
        assertFalse(policy.isAllowed());
    }

    @Test
    void wmiReaderCanOnlyRunQueries() {
        authenticate("ROLE_READER", "ROLE_WMI_READER");
        request("POST", "/api/wmi/execute");
        assertTrue(policy.isAllowed());
        request("POST", "/api/wmi/invoke");
        assertFalse(policy.isAllowed());
        request("POST", "/api/wmi/command");
        assertFalse(policy.isAllowed());
    }

    private void authenticate(String... roles) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "reader", null, Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()));
    }

    private void request(String method, String path) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(method, path)));
    }
}
