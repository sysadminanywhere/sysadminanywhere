package com.sysadminanywhere.common.incident.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentItem {

    private Long id;
    private String signalId;
    private String name;
    private Severity severity;
    private IncidentStatus status;
    private Integer eventCount;
    private String recommendation;
    private String context;
    private String affectedUser;
    private String deduplicationKey;
    private Boolean meta;
    private LocalDateTime firstEventTime;
    private LocalDateTime lastEventTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String closedBy;
    private LocalDateTime closedAt;
    private String machineName;

}
