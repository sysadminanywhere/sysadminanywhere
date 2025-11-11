package com.sysadminanywhere.client;

import com.sysadminanywhere.config.N8nFeignConfig;
import com.sysadminanywhere.model.workflow.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "n8nClient",
        url = "${n8n.url:http://localhost:5678}",
        configuration = N8nFeignConfig.class
)
public interface N8nClient {

    @GetMapping("/api/v1/workflows")
    WorkflowListResponse getWorkflows();

    @PostMapping("/api/v1/workflows/{workflowId}/execute")
    ExecuteWorkflowResponse executeWorkflow(
            @PathVariable("workflowId") String workflowId,
            @RequestBody ExecuteWorkflowRequest request
    );

    @GetMapping("/api/v1/executions/{executionId}")
    WorkflowExecution getExecution(@PathVariable("executionId") String executionId);

    @GetMapping("/api/v1/workflows/{workflowId}/executions")
    List<WorkflowExecution> getExecutions(@PathVariable("workflowId") String workflowId);

    @GetMapping("/api/v1/workflows/{workflowId}")
    WorkflowData getWorkflow(@PathVariable("workflowId") String workflowId);

    @PostMapping("/api/v1/workflows")
    Workflow createWorkflow(@RequestBody Workflow workflow);

    @PutMapping("/api/v1/workflows/{workflowId}")
    Workflow updateWorkflow(
            @PathVariable("workflowId") String workflowId,
            @RequestBody Workflow workflow
    );

    @DeleteMapping("/api/v1/workflows/{workflowId}")
    void deleteWorkflow(@PathVariable("workflowId") String workflowId);

    @GetMapping("/api/v1/executions")
    List<WorkflowExecution> getAllExecutions(
            @RequestParam(value = "status", required = false) String status
    );

}