package com.sysadminanywhere.inventory.security;

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
        if ("GET".equalsIgnoreCase(attributes.getRequest().getMethod()) && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_READER"))) return true;
        if (authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_API_TOKEN"))) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("SCOPE_inventory:read"));
    }
}
