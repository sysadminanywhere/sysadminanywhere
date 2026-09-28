package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreateRequest;
import com.sysadminanywhere.common.directory.dto.ApiTokenCreatedResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenIntrospectionResponse;
import com.sysadminanywhere.common.directory.dto.ApiTokenSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ApiTokenService {
    public static final Set<String> ALLOWED_SCOPES = Set.of(
            "directory:read", "directory:write", "remote:execute", "inventory:read", "incidents:read", "incidents:write");
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final Path configPath;

    public ApiTokenService(ObjectMapper objectMapper, JwtService jwtService,
                           @Value("${directory.api-tokens.path:${user.dir}/data/api-tokens.json}") String configPath) {
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.configPath = Paths.get(configPath);
    }

    public synchronized List<ApiTokenSummary> list() {
        return read().stream().map(TokenRecord::summary).toList();
    }

    public synchronized ApiTokenCreatedResponse create(ApiTokenCreateRequest request, String subject) {
        if (request == null || request.name() == null || request.name().isBlank() || request.name().length() > 100) {
            throw new IllegalArgumentException("Token name must contain 1 to 100 characters");
        }
        if (request.scopes() == null || request.scopes().isEmpty()
                || request.scopes().stream().anyMatch(scope -> scope == null || !ALLOWED_SCOPES.contains(scope))
                || request.scopes().stream().distinct().count() != request.scopes().size()) {
            throw new IllegalArgumentException("At least one supported scope must be selected");
        }
        int expiresInDays = request.expiresInDays() == null ? 30 : request.expiresInDays();
        if (expiresInDays < 1 || expiresInDays > 365) {
            throw new IllegalArgumentException("Token expiry must be between 1 and 365 days");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(expiresInDays);
        String id = UUID.randomUUID().toString();
        List<String> scopes = request.scopes().stream().sorted().toList();
        String token = jwtService.generateApiToken(subject, id, scopes,
                Date.from(expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        TokenRecord record = new TokenRecord(id, request.name().trim(), scopes, now, expiresAt,
                fingerprint(token), false);
        List<TokenRecord> records = read();
        records.add(record);
        write(records);
        return new ApiTokenCreatedResponse(record.summary(), token);
    }

    public synchronized boolean revoke(String id) {
        List<TokenRecord> records = read();
        boolean changed = false;
        List<TokenRecord> updated = new ArrayList<>(records.size());
        for (TokenRecord record : records) {
            if (record.id().equals(id) && !record.revoked()) {
                updated.add(new TokenRecord(record.id(), record.name(), record.scopes(), record.createdAt(),
                        record.expiresAt(), record.fingerprint(), true));
                changed = true;
            } else {
                updated.add(record);
            }
        }
        if (changed) write(updated);
        return changed;
    }

    public synchronized boolean isActive(String token, String tokenId, List<String> scopes) {
        if (token == null || tokenId == null || scopes == null) return false;
        String fingerprint = fingerprint(token);
        return read().stream().anyMatch(record -> record.id().equals(tokenId)
                && !record.revoked()
                && record.expiresAt().isAfter(LocalDateTime.now())
                && record.scopes().equals(scopes)
                && MessageDigest.isEqual(record.fingerprint().getBytes(StandardCharsets.US_ASCII),
                fingerprint.getBytes(StandardCharsets.US_ASCII)));
    }

    public ApiTokenIntrospectionResponse introspect(String token, JwtService.JwtPrincipal principal) {
        boolean active = principal.apiToken() && isActive(token, principal.tokenId(), principal.scopes());
        return new ApiTokenIntrospectionResponse(active, principal.tokenId(), principal.username(), principal.scopes());
    }

    private List<TokenRecord> read() {
        if (!Files.exists(configPath)) return new ArrayList<>();
        try {
            List<TokenRecord> records = objectMapper.readValue(configPath.toFile(), new TypeReference<>() {});
            return records == null ? new ArrayList<>() : records;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to read API token configuration", exception);
        }
    }

    private void write(List<TokenRecord> records) {
        try {
            if (configPath.getParent() != null) Files.createDirectories(configPath.getParent());
            Path parent = configPath.getParent() == null ? Paths.get(".") : configPath.getParent();
            Path temporary = Files.createTempFile(parent, "api-tokens", ".tmp");
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), records);
                try {
                    Files.move(temporary, configPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist API token configuration", exception);
        }
    }

    private String fingerprint(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private record TokenRecord(String id, String name, List<String> scopes,
                               LocalDateTime createdAt, LocalDateTime expiresAt,
                               String fingerprint, boolean revoked) {
        private ApiTokenSummary summary() {
            return new ApiTokenSummary(id, name, scopes, createdAt, expiresAt, revoked,
                    !revoked && expiresAt.isAfter(LocalDateTime.now()));
        }

    }
}
