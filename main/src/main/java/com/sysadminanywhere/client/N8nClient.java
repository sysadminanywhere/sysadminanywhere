package com.sysadminanywhere.client;

import com.sysadminanywhere.model.workflow.*;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface N8nClient {

    @GetExchange("/api/v1/workflows")
    WorkflowListResponse getWorkflows();

    @GetExchange("/api/v1/executions/{executionId}")
    Execution getExecution(String executionId);

    @GetExchange("/api/v1/executions")
    ExecutionListResponse getExecutions(String workflowId, Integer limit);

    @GetExchange("/api/v1/executions")
    ExecutionListResponse getExecutions(String workflowId, String status, Integer limit);

    @GetExchange("/api/v1/workflows/{workflowId}")
    WorkflowData getWorkflow(String workflowId);

    @DeleteExchange("/api/v1/workflows/{workflowId}")
    void deleteWorkflow(String workflowId);

    @GetExchange("/api/v1/executions")
    List<Execution> getAllExecutions(String status);

}
