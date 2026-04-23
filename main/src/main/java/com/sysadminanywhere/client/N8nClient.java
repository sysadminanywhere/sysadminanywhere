package com.sysadminanywhere.client;

import com.sysadminanywhere.model.workflow.*;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface N8nClient {

    @GetExchange("/api/v1/workflows")
    Object getWorkflows();

    @GetExchange("/api/v1/executions/{executionId}")
    Object getExecution(String executionId);

    @GetExchange("/api/v1/executions")
    Object getExecutions(String workflowId, Integer limit);

    @GetExchange("/api/v1/executions")
    Object getExecutions(String workflowId, String status, Integer limit);

    @GetExchange("/api/v1/workflows/{workflowId}")
    Object getWorkflow(String workflowId);

    @DeleteExchange("/api/v1/workflows/{workflowId}")
    void deleteWorkflow(String workflowId);

    @GetExchange("/api/v1/executions")
    Object getAllExecutions(String status);

    @PostExchange("/api/v1/workflows/{workflowId}/execute")
    Object executeWorkflow(String workflowId);

}
