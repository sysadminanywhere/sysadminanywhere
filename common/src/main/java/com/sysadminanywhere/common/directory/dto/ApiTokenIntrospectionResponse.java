package com.sysadminanywhere.common.directory.dto;

import java.util.List;

public record ApiTokenIntrospectionResponse(boolean active, String tokenId, String subject, List<String> scopes) {
}
