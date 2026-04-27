package com.sysadminanywhere.common.ai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiSearchTranslation {

    private String ldapFilter;

    private Map<String, String> inventoryFilters;

    private List<String> keywords;

    private String explanation;
}
