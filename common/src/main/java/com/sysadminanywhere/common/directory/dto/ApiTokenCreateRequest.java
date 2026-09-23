package com.sysadminanywhere.common.directory.dto;

import java.util.List;

public record ApiTokenCreateRequest(String name, List<String> scopes, Integer expiresInDays) {
}
