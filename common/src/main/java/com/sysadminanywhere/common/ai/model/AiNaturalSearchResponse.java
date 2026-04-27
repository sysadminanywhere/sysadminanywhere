package com.sysadminanywhere.common.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiNaturalSearchResponse {

    private AiSearchTranslation translation;

    private boolean success;

    private String errorMessage;
}
