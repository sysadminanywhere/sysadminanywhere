package com.sysadminanywhere.common.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiNaturalSearchRequest {

    @NotBlank
    private String query;

    private String locale;

    private String objectType; // users, computers, groups, etc.
}
