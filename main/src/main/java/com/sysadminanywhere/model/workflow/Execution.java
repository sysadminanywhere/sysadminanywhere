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
public class Execution {

    private String id;
    private String workflowId;
    private String status;
    private String errorMessage;

    private Instant startedAt;
    private Instant stoppedAt;

}