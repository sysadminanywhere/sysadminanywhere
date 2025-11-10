package com.sysadminanywhere.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteWorkflowRequest {

    @Builder.Default
    private Map<String, Object> inputData = new HashMap<>();

    @Builder.Default
    private Boolean returnExecutionData = false;

    @Builder.Default
    private String triggeredBy = "vaadin-app";

}