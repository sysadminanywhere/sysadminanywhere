package com.sysadminanywhere.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    private String id;
    private String workflowId;
    private String workflowName;
    private ExecutionStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private Map<String, Object> data;
    private String errorMessage;
    private Integer nodesExecuted;

    public enum ExecutionStatus {
        RUNNING, SUCCESS, ERROR, WAITING
    }

}