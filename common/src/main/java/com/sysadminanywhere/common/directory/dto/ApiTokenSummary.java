package com.sysadminanywhere.common.directory.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiTokenSummary(String id, String name, List<String> scopes,
                              LocalDateTime createdAt, LocalDateTime expiresAt, boolean revoked, boolean active) {
}
