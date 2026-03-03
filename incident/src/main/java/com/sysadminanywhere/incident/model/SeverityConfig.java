package com.sysadminanywhere.incident.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeverityConfig {

    @JsonProperty("default")
    private com.sysadminanywhere.common.incident.model.Severity defaultSeverity;

    private List<SeverityRule> rules;

}