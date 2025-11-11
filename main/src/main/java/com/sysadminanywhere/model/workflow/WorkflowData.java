package com.sysadminanywhere.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowData {

    private String id;
    private String name;
    private String description;
    private Boolean active;
    private Boolean isArchived;
    private List<WorkflowNode> nodes;
    private Map<String, Object> connections;
    private Map<String, Object> settings;
    private Object staticData;
    private Object meta;
    private Map<String, Object> pinData;
    private String versionId;
    private Integer triggerCount;
    private List<SharedInfo> shared;
    private List<String> tags;

}