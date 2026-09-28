package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.ApiTokenCreateRequest;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreatedResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenIntrospectionResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenSummary;
import com.sysadminanywhere.directory.service.ApiTokenService;
import com.sysadminanywhere.directory.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/api-tokens")
public class ApiTokenController {
    private final ApiTokenService apiTokenService;
    private final JwtService jwtService;

    public ApiTokenController(ApiTokenService apiTokenService, JwtService jwtService) {
        this.apiTokenService = apiTokenService;
        this.jwtService = jwtService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApiTokenSummary> list() {
        return apiTokenService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody ApiTokenCreateRequest request, Authentication authentication) {
        try {
            ApiTokenCreatedResponse created = apiTokenService.create(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revoke(@PathVariable String id) {
        return apiTokenService.revoke(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/introspect")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Validate an API token for an internal service", security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiTokenIntrospectionResponse> introspect(
            @RequestHeader("Authorization") String authorization) {
        if (!authorization.startsWith("Bearer ")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token = authorization.substring(7);
        JwtService.JwtPrincipal principal = jwtService.parseAndValidate(token);
        ApiTokenIntrospectionResponse result = apiTokenService.introspect(token, principal);
        return result.active() ? ResponseEntity.ok(result) : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
