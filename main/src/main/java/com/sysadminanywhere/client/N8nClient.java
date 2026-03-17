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

    @GetMapping("/api/v1/executions/{executionId}")
    Execution getExecution(@PathVariable("executionId") String executionId);

    @GetMapping("/api/v1/executions")
    ExecutionListResponse getExecutions(
            @RequestParam("workflowId") String workflowId,
            @RequestParam(value = "limit", required = false) Integer limit
    );

    @GetMapping("/api/v1/executions")
    ExecutionListResponse getExecutions(
            @RequestParam(value = "workflowId", required = false) String workflowId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "limit", required = false) Integer limit
    );

    @GetMapping("/api/v1/workflows/{workflowId}")
    WorkflowData getWorkflow(@PathVariable("workflowId") String workflowId);

    @DeleteMapping("/api/v1/workflows/{workflowId}")
    void deleteWorkflow(@PathVariable("workflowId") String workflowId);

    @GetMapping("/api/v1/executions")
    List<Execution> getAllExecutions(
            @RequestParam(value = "status", required = false) String status
    );

}