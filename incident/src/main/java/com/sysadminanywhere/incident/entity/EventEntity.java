package com.sysadminanywhere.incident.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events",
        indexes = {
                @Index(name = "idx_events_processed", columnList = "processed"),
                @Index(name = "idx_events_recordId", columnList = "recordId"),
                @Index(name = "idx_events_incidentId", columnList = "incidentId"),
                @Index(name = "idx_events_event_time", columnList = "eventId, timeCreated"),
                @Index(name = "idx_events_incident_time", columnList = "incidentId, timeCreated")
        }
)
public class EventEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long recordId;

    private Integer eventId;

    private LocalDateTime timeCreated;

    private String machineName;

    private String levelDisplayName;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String extra;

    private Boolean processed = false;

    private Long incidentId;

}