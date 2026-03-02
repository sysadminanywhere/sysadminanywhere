package com.sysadminanywhere.incident.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeverityConfig {

    @JsonProperty("default")
    private Severity defaultSeverity;

    private List<SeverityRule> rules;

}