package com.sysadminanywhere.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNode {

    private String id;
    private String name;
    private String type;
    private Integer typeVersion;
    private List<Double> position;
    private Map<String, Object> parameters;
    private String webhookId;

}