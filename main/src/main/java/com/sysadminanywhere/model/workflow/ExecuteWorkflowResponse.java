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
public class ExecuteWorkflowResponse {

    private String executionId;
    private String workflowId;
    private String status;
    private String message;
    private Object resultData;
    private Instant startedAt;
    private Instant finishedAt;

}