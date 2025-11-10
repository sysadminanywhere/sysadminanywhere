package com.sysadminanywhere.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowInfo {

    private String id;
    private String name;
    private String description;
    private Boolean active;
    private Instant createdAt;
    private Instant lastExecuted;
    private String triggerType;
    private Integer executionCount;

}