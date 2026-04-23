package com.sysadminanywhere.client;

import com.sysadminanywhere.model.workflow.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface N8nClient {

    @GetExchange("/api/v1/workflows")
    WorkflowListResponse getWorkflows();

    @GetExchange("/api/v1/executions/{executionId}")
    Execution getExecution(@PathVariable String executionId);

    @GetExchange("/api/v1/executions")
    ExecutionListResponse getExecutions(@RequestParam String workflowId, @RequestParam Integer limit);

    @GetExchange("/api/v1/executions")
    ExecutionListResponse getExecutions(@RequestParam String workflowId, @RequestParam String status, @RequestParam Integer limit);

    @GetExchange("/api/v1/workflows/{workflowId}")
    WorkflowData getWorkflow(@PathVariable String workflowId);

    @DeleteExchange("/api/v1/workflows/{workflowId}")
    void deleteWorkflow(@PathVariable String workflowId);

    @GetExchange("/api/v1/executions")
    List<Execution> getAllExecutions(@RequestParam String status);

    @PostExchange("/api/v1/workflows/{workflowId}/execute")
    String executeWorkflow(@PathVariable String workflowId);

}
