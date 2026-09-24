package com.sysadminanywhere.directory.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component("apiTokenAuthorization")
public class ApiTokenAuthorization {
    public boolean isAllowed() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return false;
        HttpServletRequest request = attributes.getRequest();
        String path = request.getRequestURI();
        if (path.startsWith("/api/wmi")) {
            if ("POST".equalsIgnoreCase(request.getMethod()) && path.equals("/api/wmi/execute")
                    && authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_WMI_READER"))) return true;
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("SCOPE_remote:execute"));
        }
        boolean readOnlyRequest = "GET".equalsIgnoreCase(request.getMethod())
                || "POST".equalsIgnoreCase(request.getMethod())
                && (path.equals("/api/ldap/search") || path.equals("/api/ldap/count"));
        if (readOnlyRequest && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_READER"))) return true;
        if (authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_API_TOKEN"))) {
            return false;
        }
        String requiredScope = readOnlyRequest ? "SCOPE_directory:read" : "SCOPE_directory:write";
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredScope));
    }
}
